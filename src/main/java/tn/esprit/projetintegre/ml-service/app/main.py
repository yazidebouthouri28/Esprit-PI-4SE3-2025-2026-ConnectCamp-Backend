"""
FastAPI — Camping Price Prediction  (compatible nouveau format d'artifact)
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List
import numpy as np, pandas as pd, joblib, json, os, logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

BASE_DIR   = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR   = os.path.dirname(BASE_DIR)
MODEL_PATH = os.path.join(ROOT_DIR, 'model', 'price_model.joblib')
META_PATH  = os.path.join(ROOT_DIR, 'model', 'model_meta.json')

# Artifact global (contient model + encoders)
artifact  = None
model_meta = {}


def load_model():
    global artifact, model_meta
    logger.info(f'Looking for model at: {MODEL_PATH}')
    if not os.path.exists(MODEL_PATH):
        logger.warning(f'Model not found at {MODEL_PATH}')
        return
    artifact = joblib.load(MODEL_PATH)
    if os.path.exists(META_PATH):
        with open(META_PATH) as f:
            model_meta = json.load(f)
    logger.info(f'Model loaded OK. R2={model_meta.get("r2")}  MAPE={model_meta.get("mape")}%')


app = FastAPI(title='Camping Price Prediction API', version='2.0.0')
app.add_middleware(
    CORSMiddleware, allow_origins=['*'], allow_methods=['*'], allow_headers=['*']
)


@app.on_event('startup')
def startup():
    load_model()


# ──────────────────────────────────────────────────────────────────────────────
# Input schema
# ──────────────────────────────────────────────────────────────────────────────
class ProductInput(BaseModel):
    name:               Optional[str]        = None
    brand:              Optional[str]        = 'Unknown'
    categoryName:       Optional[str]        = 'Outdoor Accessories'
    weight:             Optional[float]      = 0.0
    dimensions:         Optional[str]        = None
    stockQuantity:      Optional[int]        = 0
    minStockLevel:      Optional[int]        = 0
    rating:             Optional[float]      = 3.0
    reviewCount:        Optional[int]        = 0
    salesCount:         Optional[int]        = 0
    viewCount:          Optional[int]        = 0
    isFeatured:         Optional[bool]       = False
    isOnSale:           Optional[bool]       = False
    isRentable:         Optional[bool]       = False
    rentalPricePerDay:  Optional[float]      = 0.0
    tags:               Optional[List[str]]  = []
    imagesCount:        Optional[int]        = 1
    competitorPrice:    Optional[float]      = 0.0
    supplierCost:       Optional[float]      = 0.0
    shippingCost:       Optional[float]      = 0.0


# ──────────────────────────────────────────────────────────────────────────────
# Feature engineering (miroir exact du training)
# ──────────────────────────────────────────────────────────────────────────────
CATEGORICAL  = ['brand', 'categoryName']
NUMERIC_BASE = [
    'weight', 'stockQuantity', 'minStockLevel', 'rating', 'reviewCount',
    'salesCount', 'viewCount', 'imagesCount', 'rentalPricePerDay',
    'competitorPrice', 'supplierCost', 'shippingCost',
]
BOOLEAN = ['isFeatured', 'isOnSale', 'isRentable']

ENGINEERED = [
    'volume', 'logVolume', 'tagCount',
    'priceToSupplierRatio', 'marginProxy', 'shippingToSupplierRatio',
    'competitorDelta', 'competitorDeltaPct', 'hasCompetitor',
    'demandIndex', 'logSales', 'logViews', 'logReviews',
    'conversionRate', 'reviewsPerSale', 'ratingWeighted',
    'rentalRatio', 'stockCoverage',
]
ALL_FEATURES = CATEGORICAL + NUMERIC_BASE + BOOLEAN + ENGINEERED
# Mapping catégories custom → catégories du modèle
CATEGORY_MAP = {
    'tent':         'Tents',
    'tents':        'Tents',
    'camp':         'Tents',
    'outdoor':      'Outdoor Accessories',
    'indoor':       'Outdoor Accessories',
    'sport':        'Backpacks',
    'night':        'Lanterns',
    'day':          'Outdoor Accessories',
    'nature':       'Outdoor Accessories',
    'sleeping':     'Sleeping Bags',
    'backpack':     'Backpacks',
    'stove':        'Portable Stoves',
    'chair':        'Camping Chairs',
    'lantern':      'Lanterns',
}

def normalize_category(cat: str) -> str:
    if not cat:
        return 'Outdoor Accessories'
    key = cat.lower().strip()
    # Cherche si un mot-clé connu est dans le nom
    for k, v in CATEGORY_MAP.items():
        if k in key:
            return v
    return 'Outdoor Accessories'  # fallback

def to_df(d: dict) -> pd.DataFrame:
    # ── raw fields ──
    tags       = d.get('tags') or []
    tag_count  = len(tags)
    sc         = float(d.get('supplierCost')    or 0)
    cp         = float(d.get('competitorPrice') or 0)
    sl         = int(d.get('salesCount')        or 0)
    rt         = float(d.get('rating')          or 3.0)
    vw         = int(d.get('viewCount')         or 0)
    rv         = int(d.get('reviewCount')       or 0)
    rpd        = float(d.get('rentalPricePerDay') or 0)
    ship       = float(d.get('shippingCost')    or 0)
    sq         = int(d.get('stockQuantity')     or 0)
    msl        = int(d.get('minStockLevel')     or 0)


    # ── volume ──
    dims = d.get('dimensions')
    volume = np.nan
    if dims:
        try:
            p = str(dims).split('x')
            if len(p) == 3:
                volume = float(p[0]) * float(p[1]) * float(p[2])
        except Exception:
            pass

    sc_safe  = max(sc, 1)
    sl_safe  = max(sl, 1)
    vw_safe  = max(vw, 1)
    msl_safe = max(msl, 1)

    row = {
        # categoricals
        'brand':        d.get('brand')        or 'Unknown',
'categoryName': normalize_category(d.get('categoryName')),        # numerics
        'weight':            float(d.get('weight')       or 0),
        'stockQuantity':     sq,
        'minStockLevel':     msl,
        'rating':            rt,
        'reviewCount':       rv,
        'salesCount':        sl,
        'viewCount':         vw,
        'imagesCount':       int(d.get('imagesCount')    or 1),
        'rentalPricePerDay': rpd,
        'competitorPrice':   cp,
        'supplierCost':      sc,
        'shippingCost':      ship,
        # booleans
        'isFeatured':  int(bool(d.get('isFeatured'))),
        'isOnSale':    int(bool(d.get('isOnSale'))),
        'isRentable':  int(bool(d.get('isRentable'))),
        # engineered
        'volume':                  volume,
        'logVolume':               np.log1p(volume if not np.isnan(volume) else 0),
        'tagCount':                tag_count,
        'priceToSupplierRatio':    sc_safe,
        'marginProxy':             (cp - sc) / sc_safe if cp > 0 else 0,
        'shippingToSupplierRatio': ship / sc_safe,
        'competitorDelta':         (cp - sc) if cp > 0 else 0,
        'competitorDeltaPct':      (cp - sc) / sc_safe if cp > 0 else 0,
        'hasCompetitor':           1 if cp > 0 else 0,
        'demandIndex':             np.log1p(sl) * rt,
        'logSales':                np.log1p(sl),
        'logViews':                np.log1p(vw),
        'logReviews':              np.log1p(rv),
        'conversionRate':          sl / vw_safe,
        'reviewsPerSale':          rv / sl_safe,
        'ratingWeighted':          rt * np.log1p(rv),
        'rentalRatio':             rpd / sc_safe,
        'stockCoverage':           sq / msl_safe,
    }
    return pd.DataFrame([row])


# ──────────────────────────────────────────────────────────────────────────────
# Endpoints
# ──────────────────────────────────────────────────────────────────────────────
@app.get('/health')
def health():
    return {
        'status':      'UP' if artifact else 'DEGRADED',
        'modelLoaded': artifact is not None,
        'modelPath':   MODEL_PATH,
        'modelExists': os.path.exists(MODEL_PATH),
        'engine':      model_meta.get('engine', 'unknown'),
        'r2':          model_meta.get('r2'),
        'mape':        model_meta.get('mape'),
    }


@app.post('/predict-price')
def predict(product: ProductInput):
    if not artifact:
        raise HTTPException(503, 'Model not loaded')
    try:
        df = to_df(product.model_dump())

        enc = artifact['encoder']
        imp = artifact['imputer']
        mdl = artifact['model']

        # Encode categoricals
        df[CATEGORICAL] = enc.transform(df[CATEGORICAL])

        # Impute numerics
        num_cols = NUMERIC_BASE + ENGINEERED
        df[num_cols] = imp.transform(df[num_cols])

        pred = round(float(mdl.predict(df[ALL_FEATURES])[0]), 2)
        mae  = model_meta.get('mae', pred * 0.1)

        # Confidence basée sur les données disponibles
        has_data = sum([
            (product.competitorPrice or 0) > 0,
            (product.supplierCost    or 0) > 0,
            (product.reviewCount     or 0) > 10,
            (product.salesCount      or 0) > 0,
        ])
        confidence = ['low', 'low', 'medium', 'high', 'high'][has_data]

        return {
            'predictedPrice': pred,
            'confidence':     confidence,
            'priceRange': {
                'min': round(max(0.01, pred - mae), 2),
                'max': round(pred + mae, 2),
            },
            'modelMetrics': model_meta,
        }
    except Exception as e:
        logger.exception('Prediction error')
        raise HTTPException(500, str(e))


@app.get('/model/info')
def info():
    return model_meta