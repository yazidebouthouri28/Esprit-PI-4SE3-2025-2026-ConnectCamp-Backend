import pandas as pd
import numpy as np
import random
import json

np.random.seed(42)
random.seed(42)

CATEGORIES = {
    'Tents':    (50, 800),
    'outdoor':  (10, 300),
    'camp':     (20, 500),
    'sport':    (30, 400),
    'indoor':   (15, 200),
    'night':    (10, 150),
    'day':      (10, 200),
    'nature':   (20, 350),
}

WEIGHT_RANGES = {
    'Tents':    (1.5, 8.0),
    'outdoor':  (0.1, 3.0),
    'camp':     (0.5, 6.0),
    'sport':    (0.3, 4.0),
    'indoor':   (0.1, 2.0),
    'night':    (0.1, 1.5),
    'day':      (0.1, 2.0),
    'nature':   (0.2, 3.0),
}

BRANDS = {
    'premium': ['The North Face', 'Patagonia', 'MSR', 'Black Diamond'],
    'mid':     ['Coleman', 'Quechua', 'Marmot', 'Osprey'],
    'budget':  ['Ozark Trail', 'Teton Sports', 'Wakeman', 'Stansport'],
}

TAGS_POOL = ['waterproof','lightweight','4-season','ultralight','family',
             'solo','hiking','backpacking','car-camping','durable',
             'compact','foldable','eco-friendly','windproof','UV-resistant']

def get_brand_tier(brand):
    for tier, brands in BRANDS.items():
        if brand in brands:
            return tier
    return 'mid'

def generate_dimensions(category):
    # toutes les catégories custom → dimensions generiques
    default = lambda: f'{random.randint(10,50)}x{random.randint(10,40)}x{random.randint(5,30)}'
    dims = {
        'Tents': lambda: f'{random.randint(200,300)}x{random.randint(150,280)}x{random.randint(100,200)}',
    }
    return dims.get(category, default)()

def generate_price(category, brand, rating, sales_count, competitor_price, supplier_cost, is_featured, is_on_sale):
    base_min, base_max = CATEGORIES[category]
    tier = get_brand_tier(brand)
    tier_multiplier = {'premium': 1.6, 'mid': 1.0, 'budget': 0.6}[tier]
    rating_factor   = 1 + (rating - 3.0) * 0.08
    demand_factor   = 1 + min(sales_count / 5000, 0.3)
    featured_factor = 1.05 if is_featured else 1.0
    base_price = (base_min + base_max) / 2 * tier_multiplier * rating_factor * demand_factor * featured_factor
    base_price = max(supplier_cost * 1.4, base_price)
    if competitor_price > 0:
        base_price = base_price * 0.7 + competitor_price * 0.3
    if is_on_sale:
        base_price *= random.uniform(0.75, 0.90)
    noise = np.random.normal(0, base_price * 0.05)
    return round(max(supplier_cost * 1.2, base_price + noise), 2)

def generate_dataset(n=3000):
    rows = []
    category_ids = {cat: idx+1 for idx, cat in enumerate(CATEGORIES)}
    all_brands   = [b for tier in BRANDS.values() for b in tier]

    for i in range(n):
        category_name  = random.choice(list(CATEGORIES.keys()))
        brand          = random.choice(all_brands)
        tier           = get_brand_tier(brand)
        rating         = round(random.uniform(2.5, 5.0), 1)
        review_count   = int(np.random.exponential(80))
        sales_count    = int(np.random.exponential(300))
        view_count     = sales_count * random.randint(5, 20)
        stock_qty      = random.randint(0, 200)
        is_featured    = random.random() < 0.15
        is_on_sale     = random.random() < 0.20
        is_rentable    = category_name in ['Tents', 'camp', 'outdoor'] and random.random() < 0.4

        w_min, w_max   = WEIGHT_RANGES[category_name]
        weight         = round(random.uniform(w_min, w_max), 2)

        base_min, base_max = CATEGORIES[category_name]
        est_price      = (base_min + base_max) / 2 * {'premium':1.6,'mid':1.0,'budget':0.6}[tier]
        supplier_cost  = round(est_price * {'premium':0.45,'mid':0.38,'budget':0.30}[tier] * random.uniform(0.85, 1.15), 2)
        shipping_cost  = round(weight * random.uniform(1.5, 3.5) + random.uniform(2, 8), 2)
        competitor_price = round(est_price * random.uniform(0.85, 1.20), 2) if random.random() < 0.8 else 0.0
        rental_price   = round(est_price * 0.08, 2) if is_rentable else 0.0
        tags           = random.sample(TAGS_POOL, random.randint(1, 4))

        price = generate_price(category_name, brand, rating, sales_count,
                               competitor_price, supplier_cost, is_featured, is_on_sale)

        rows.append({
            'name':             f'{brand} {category_name} {i}',
            'brand':            brand,
            'categoryId':       category_ids[category_name],
            'categoryName':     category_name,
            'weight':           weight,
            'dimensions':       generate_dimensions(category_name),
            'stockQuantity':    stock_qty,
            'minStockLevel':    random.randint(5, 30),
            'rating':           rating,
            'reviewCount':      review_count,
            'salesCount':       sales_count,
            'viewCount':        view_count,
            'isFeatured':       is_featured,
            'isOnSale':         is_on_sale,
            'isRentable':       is_rentable,
            'rentalPricePerDay':rental_price,
            'tags':             json.dumps(tags),
            'imagesCount':      random.randint(1, 8),
            'competitorPrice':  competitor_price,
            'supplierCost':     supplier_cost,
            'shippingCost':     shipping_cost,
            'price':            price,
        })

    return pd.DataFrame(rows)


if __name__ == '__main__':
    df = generate_dataset(3000)
    df.to_csv('data/camping_products.csv', index=False)
    print(f'Dataset generated: {len(df)} rows')
    print(df.groupby('categoryName')['price'].describe().round(2))