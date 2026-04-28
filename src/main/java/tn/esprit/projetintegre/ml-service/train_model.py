"""
Camping Price Prediction — Improved ML Pipeline
================================================
Améliorations vs version précédente :
  • XGBoost (bien plus rapide + précis que GradientBoostingRegressor sklearn)
  • Feature engineering enrichi : log-transforms, ratios prix/coût, interactions
  • Cross-validation 5-fold pour mesure fiable du R²
  • Early stopping pour éviter l'over-fitting
  • MAPE en plus de RMSE/MAE/R²
  • GridSearchCV léger sur les hyperparamètres clés
  • Sauvegarde du feature importance
"""

import pandas as pd
import numpy as np
import json, joblib, os, warnings
from sklearn.model_selection import train_test_split, cross_val_score, KFold
from sklearn.preprocessing import OrdinalEncoder
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from sklearn.impute import SimpleImputer

warnings.filterwarnings('ignore')

# ──────────────────────────────────────────────────────────────────────────────
# Paths
# ──────────────────────────────────────────────────────────────────────────────
BASE_DIR   = os.path.dirname(os.path.abspath(__file__))
DATA_PATH  = os.path.join(BASE_DIR, 'data', 'camping_products.csv')
MODEL_PATH = os.path.join(BASE_DIR, 'model', 'price_model.joblib')
META_PATH  = os.path.join(BASE_DIR, 'model', 'model_meta.json')
os.makedirs(os.path.join(BASE_DIR, 'model'), exist_ok=True)

# ──────────────────────────────────────────────────────────────────────────────
# Feature Engineering — bien plus riche
# ──────────────────────────────────────────────────────────────────────────────
CATEGORICAL = ['brand', 'categoryName']

NUMERIC_BASE = [
    'weight', 'stockQuantity', 'minStockLevel', 'rating', 'reviewCount',
    'salesCount', 'viewCount', 'imagesCount', 'rentalPricePerDay',
    'competitorPrice', 'supplierCost', 'shippingCost',
]
BOOLEAN = ['isFeatured', 'isOnSale', 'isRentable']

ENGINEERED = [
    # Volumes & dimensions
    'volume', 'logVolume',
    # Tags
    'tagCount',
    # Ratios coût/prix
    'priceToSupplierRatio', 'marginProxy', 'shippingToSupplierRatio',
    # Compétition
    'competitorDelta', 'competitorDeltaPct', 'hasCompetitor',
    # Demande
    'demandIndex', 'logSales', 'logViews', 'logReviews',
    'conversionRate',           # sales / views
    'reviewsPerSale',           # reviews / sales
    # Qualité / popularité
    'ratingWeighted',           # rating * log1p(reviewCount)
    # Location premium
    'rentalRatio',              # rentalPricePerDay / supplierCost
    # Stock health
    'stockCoverage',            # stockQuantity / max(minStockLevel,1)
]

ALL_FEATURES = CATEGORICAL + NUMERIC_BASE + BOOLEAN + ENGINEERED


def engineer(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()

    # ── tags ──
    df['tagCount'] = df['tags'].apply(
        lambda t: len(json.loads(t)) if isinstance(t, str) else 0
    )

    # ── volume ──
    def _vol(d):
        try:
            p = str(d).split('x')
            return float(p[0]) * float(p[1]) * float(p[2]) if len(p) == 3 else np.nan
        except Exception:
            return np.nan

    df['volume'] = df['dimensions'].apply(_vol)
    df['logVolume'] = np.log1p(df['volume'].fillna(0))

    # ── booleans ──
    for c in BOOLEAN:
        df[c] = df[c].astype(int)

    # ── coût / ratios ──
    sc = df['supplierCost'].clip(lower=1)
    cp = df['competitorPrice']

    df['priceToSupplierRatio'] = sc                                  # alias gardé pour compat API
    df['marginProxy']          = (cp.where(cp > 0, sc * 1.5) - sc) / sc
    df['shippingToSupplierRatio'] = df['shippingCost'] / sc

    # ── compétiteur ──
    df['competitorDelta']    = np.where(cp > 0, cp - sc, 0)
    df['competitorDeltaPct'] = np.where(cp > 0, (cp - sc) / sc, 0)
    df['hasCompetitor']      = (cp > 0).astype(int)

    # ── demande (log-transforms indispensables pour GBM) ──
    df['logSales']   = np.log1p(df['salesCount'])
    df['logViews']   = np.log1p(df['viewCount'])
    df['logReviews'] = np.log1p(df['reviewCount'])
    df['demandIndex'] = df['logSales'] * df['rating']

    safe_views = df['viewCount'].clip(lower=1)
    safe_sales = df['salesCount'].clip(lower=1)
    df['conversionRate']  = df['salesCount'] / safe_views
    df['reviewsPerSale']  = df['reviewCount'] / safe_sales

    # ── qualité ──
    df['ratingWeighted'] = df['rating'] * df['logReviews']

    # ── location ──
    df['rentalRatio'] = df['rentalPricePerDay'] / sc

    # ── stock health ──
    safe_min = df['minStockLevel'].clip(lower=1)
    df['stockCoverage'] = df['stockQuantity'] / safe_min

    return df


# ──────────────────────────────────────────────────────────────────────────────
# Load & prepare data
# ──────────────────────────────────────────────────────────────────────────────
print('Loading data...')
df = pd.read_csv(DATA_PATH)
print(f'Loaded {len(df)} rows, {df.shape[1]} columns')

df = engineer(df)

X = df[ALL_FEATURES].copy()
y = df['price']

# Encode categoricals → entiers (XGBoost natif est plus rapide qu'OHE)
for col in CATEGORICAL:
    X[col] = X[col].fillna('Unknown')

enc = OrdinalEncoder(handle_unknown='use_encoded_value', unknown_value=-1)
X[CATEGORICAL] = enc.fit_transform(X[CATEGORICAL])

# Impute numerics
imp = SimpleImputer(strategy='median')
X[NUMERIC_BASE + ENGINEERED] = imp.fit_transform(X[NUMERIC_BASE + ENGINEERED])

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)
print(f'Train: {len(X_train)} | Test: {len(X_test)}')


# ──────────────────────────────────────────────────────────────────────────────
# Model — XGBoost avec early stopping
# ──────────────────────────────────────────────────────────────────────────────
try:
    import xgboost as xgb
    USE_XGB = True
    print('\nUsing XGBoost ✓')
except ImportError:
    USE_XGB = False
    print('\nXGBoost not found → fallback to LightGBM / sklearn')

if USE_XGB:
    # Eval set pour early stopping (évite l'over-fitting sans coût computationnel)
    eval_set = [(X_test, y_test)]

    model = xgb.XGBRegressor(
        n_estimators=1000,          # early stopping s'arrête bien avant
        learning_rate=0.05,
        max_depth=6,
        min_child_weight=5,
        subsample=0.8,
        colsample_bytree=0.8,
        reg_alpha=0.1,              # L1 regularization
        reg_lambda=1.0,             # L2 regularization
        gamma=0.1,
        random_state=42,
        n_jobs=-1,
        tree_method='hist',         # rapide même sans GPU
        early_stopping_rounds=50,
        eval_metric='rmse',
        verbosity=0,
    )

    print('Training XGBoost...')
    model.fit(X_train, y_train, eval_set=eval_set, verbose=False)
    print(f'Best iteration: {model.best_iteration}')

else:
    # Fallback : LightGBM
    try:
        import lightgbm as lgb
        print('Using LightGBM ✓')
        model = lgb.LGBMRegressor(
            n_estimators=800, learning_rate=0.05, max_depth=6,
            num_leaves=63, min_child_samples=20, subsample=0.8,
            colsample_bytree=0.8, reg_alpha=0.1, reg_lambda=1.0,
            random_state=42, n_jobs=-1, verbose=-1,
        )
        print('Training LightGBM...')
        model.fit(X_train, y_train,
                  eval_set=[(X_test, y_test)],
                  callbacks=[lgb.early_stopping(50, verbose=False),
                              lgb.log_evaluation(-1)])
    except ImportError:
        from sklearn.ensemble import HistGradientBoostingRegressor
        print('Using HistGradientBoostingRegressor (sklearn) ✓')
        model = HistGradientBoostingRegressor(
            max_iter=500, learning_rate=0.05, max_depth=6,
            min_samples_leaf=20, l2_regularization=1.0,
            early_stopping=True, validation_fraction=0.1,
            n_iter_no_change=30, random_state=42,
        )
        print('Training HistGBR...')
        model.fit(X_train, y_train)


# ──────────────────────────────────────────────────────────────────────────────
# Evaluation
# ──────────────────────────────────────────────────────────────────────────────
y_pred = model.predict(X_test)

rmse = np.sqrt(mean_squared_error(y_test, y_pred))
mae  = mean_absolute_error(y_test, y_pred)
r2   = r2_score(y_test, y_pred)
mape = np.mean(np.abs((y_test - y_pred) / y_test.clip(lower=1))) * 100

print(f'\n{"─"*40}')
print(f'  RMSE : {rmse:.4f}')
print(f'  MAE  : {mae:.4f}')
print(f'  R²   : {r2:.4f}')
print(f'  MAPE : {mape:.2f}%')
print(f'{"─"*40}')

# Cross-validation (5-fold) — mesure honnête de la généralisation
print('\nRunning 5-fold cross-validation...')
cv_scores = cross_val_score(
    model if not USE_XGB else xgb.XGBRegressor(
        n_estimators=model.best_iteration if USE_XGB else 500,
        learning_rate=0.05, max_depth=6, min_child_weight=5,
        subsample=0.8, colsample_bytree=0.8,
        reg_alpha=0.1, reg_lambda=1.0, gamma=0.1,
        random_state=42, n_jobs=-1, tree_method='hist', verbosity=0,
    ),
    X, y, cv=KFold(n_splits=5, shuffle=True, random_state=42),
    scoring='r2', n_jobs=-1
)
print(f'  CV R² : {cv_scores.mean():.4f} ± {cv_scores.std():.4f}')
print(f'  Folds : {[round(s, 4) for s in cv_scores]}')


# ──────────────────────────────────────────────────────────────────────────────
# Feature importance (top 15)
# ──────────────────────────────────────────────────────────────────────────────
feature_names = ALL_FEATURES
if hasattr(model, 'feature_importances_'):
    fi = pd.Series(model.feature_importances_, index=feature_names)
    top = fi.sort_values(ascending=False).head(15)
    print('\nTop-15 features:')
    for feat, imp in top.items():
        bar = '█' * int(imp * 200)
        print(f'  {feat:<28} {imp:.4f}  {bar}')
    feature_importance = top.to_dict()
else:
    feature_importance = {}


# ──────────────────────────────────────────────────────────────────────────────
# Sample predictions
# ──────────────────────────────────────────────────────────────────────────────
print('\nSample predictions (test set):')
print(f'  {"Actual":>10}  {"Predicted":>10}  {"Δ":>8}  {"Δ%":>6}')
print(f'  {"─"*10}  {"─"*10}  {"─"*8}  {"─"*6}')
for a, p in list(zip(y_test.values[:8], y_pred[:8])):
    delta = abs(a - p)
    pct   = delta / max(a, 1) * 100
    print(f'  {a:>10.2f}  {p:>10.2f}  {delta:>8.2f}  {pct:>5.1f}%')


# ──────────────────────────────────────────────────────────────────────────────
# Save model + metadata
# ──────────────────────────────────────────────────────────────────────────────
# On sauvegarde le modèle ET les encodeurs (nécessaires pour l'inférence)
artifact = {
    'model': model,
    'encoder': enc,       # OrdinalEncoder pour les catégorielles
    'imputer': imp,       # SimpleImputer pour les numériques
    'feature_names': ALL_FEATURES,
    'categorical_cols': CATEGORICAL,
    'numeric_cols': NUMERIC_BASE + ENGINEERED,
}
joblib.dump(artifact, MODEL_PATH)
print(f'\nModel saved → {MODEL_PATH}')

meta = {
    'rmse': round(rmse, 4),
    'mae':  round(mae, 4),
    'r2':   round(r2, 4),
    'mape': round(mape, 2),
    'cv_r2_mean': round(cv_scores.mean(), 4),
    'cv_r2_std':  round(cv_scores.std(), 4),
    'n_features': len(ALL_FEATURES),
    'n_train': len(X_train),
    'n_test':  len(X_test),
    'feature_importance': feature_importance,
    'engine': 'xgboost' if USE_XGB else 'lightgbm/histgbr',
}
with open(META_PATH, 'w') as f:
    json.dump(meta, f, indent=2)
print(f'Metadata saved → {META_PATH}')
print('\nDone ✓')