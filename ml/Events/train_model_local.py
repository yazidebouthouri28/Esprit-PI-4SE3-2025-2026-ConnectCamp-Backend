# train_model_local.py
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
import joblib
import warnings
warnings.filterwarnings('ignore')

print("🔄 Génération du dataset enrichi...")

ALL_CATEGORIES = ['Nature', 'Adventure', 'Music', 'Sport', 'Education', 'Culture', 'Technology', 'Other']

ALL_EVENT_TYPES = ['CONCERT', 'CONFERENCE', 'EXHIBITION', 'FESTIVAL',
                   'MEETUP', 'OTHER', 'OUTDOOR_ACTIVITY', 'WORKSHOP',
                   'CAMPING', 'HIKING', 'SPORTS', 'SOCIAL']

# ✅ Toutes les villes tunisiennes courantes
ALL_STATES = [
    'Tunis', 'Ariana', 'Ben Arous', 'Manouba',
    'Bizerte', 'Nabeul', 'Hammamet', 'Sousse',
    'Monastir', 'Mahdia', 'Sfax', 'Kairouan',
    'Kasserine', 'Sidi Bouzid', 'Gabes', 'Medenine',
    'Djerba', 'Tataouine', 'Gafsa', 'Tozeur',
    'Kebili', 'Jendouba', 'Tabarka', 'Kef',
    'Siliana', 'Zaghouan', 'Beja'
]

N = 2000
np.random.seed(42)

categories  = np.random.choice(ALL_CATEGORIES,  N)
event_types = np.random.choice(ALL_EVENT_TYPES, N)
states      = np.random.choice(ALL_STATES,      N)
hours       = np.random.randint(8, 23, N)
months      = np.random.randint(1, 13, N)
days_of_week= np.random.randint(0, 7,  N)
durations   = np.random.randint(1, 12, N)
prices      = np.random.choice([0, 10, 15, 20, 25, 30, 50, 75, 100], N)

# Logique réaliste
base_cat = {
    'Music': 80, 'Sport': 65, 'Adventure': 45, 'Culture': 55,
    'Nature': 40, 'Technology': 70, 'Education': 35, 'Other': 25
}
base_type = {
    'FESTIVAL': 35, 'CONCERT': 30, 'OUTDOOR_ACTIVITY': 25,
    'CONFERENCE': 20, 'EXHIBITION': 15, 'SPORTS': 20,
    'SOCIAL': 15, 'CAMPING': 20, 'HIKING': 18,
    'MEETUP': 10, 'WORKSHOP': 8, 'OTHER': 5
}
# Grandes villes → plus de participants
city_bonus = {
    'Tunis': 20, 'Sfax': 12, 'Sousse': 12, 'Hammamet': 15,
    'Djerba': 18, 'Monastir': 10, 'Ariana': 10, 'Nabeul': 8,
    'Tabarka': 12, 'Bizerte': 8, 'Ben Arous': 7, 'Gabes': 6,
    'Kairouan': 5, 'Mahdia': 7, 'Manouba': 5, 'Jendouba': 4,
    'Gafsa': 4, 'Tozeur': 8, 'Kebili': 3, 'Tataouine': 3,
    'Medenine': 4, 'Kasserine': 3, 'Sidi Bouzid': 3,
    'Beja': 4, 'Siliana': 3, 'Zaghouan': 4, 'Kef': 3
}
price_penalty = {0: 25, 10: 15, 15: 10, 20: 5, 25: 2,
                 30: 0, 50: -10, 75: -18, 100: -28}
weekend_bonus = np.where(days_of_week >= 5, 15, 0)
evening_bonus = np.where((hours >= 18) & (hours <= 22), 12, 0)
summer_bonus  = np.where((months >= 6) & (months <= 8), 10, 0)

attendees = np.array([
    max(1, base_cat[categories[i]]
        + base_type[event_types[i]]
        + city_bonus.get(states[i], 5)
        + price_penalty.get(prices[i], 0)
        + weekend_bonus[i]
        + evening_bonus[i]
        + summer_bonus[i]
        + durations[i] * 2
        + np.random.randint(-15, 25))
    for i in range(N)
])

df = pd.DataFrame({
    'category':       categories,
    'event_type':     event_types,
    'state':          states,
    'hour':           hours,
    'month':          months,
    'day_of_week':    days_of_week,
    'duration_hours': durations,
    'price':          prices,
    'attendee_count': attendees
})

print(f"📊 Dataset : {len(df)} lignes")
print(f"   Catégories  : {sorted(df['category'].unique())}")
print(f"   Event types : {sorted(df['event_type'].unique())}")
print(f"   Villes      : {sorted(df['state'].unique())}")

# Encoders
le_category   = LabelEncoder()
le_event_type = LabelEncoder()
le_state      = LabelEncoder()

df['category_encoded']   = le_category.fit_transform(df['category'])
df['event_type_encoded'] = le_event_type.fit_transform(df['event_type'])
df['state_encoded']      = le_state.fit_transform(df['state'])

print(f"\n✅ Classes category  : {list(le_category.classes_)}")
print(f"✅ Classes event_type: {list(le_event_type.classes_)}")
print(f"✅ Classes state     : {list(le_state.classes_)}")

FEATURES = [
    'category_encoded',
    'event_type_encoded',
    'state_encoded',
    'hour',
    'month',
    'day_of_week',
    'duration_hours',
    'price'
]

X = df[FEATURES]
y = df['attendee_count']

print("\n🔄 Entraînement...")
model = RandomForestRegressor(n_estimators=200, max_depth=15, random_state=42, n_jobs=-1)
model.fit(X, y)
print("✅ Modèle entraîné !")

# Sauvegarder
joblib.dump(model,         'event_popularity_model.pkl')
joblib.dump(le_category,   'label_encoder_category.pkl')
joblib.dump(le_event_type, 'label_encoder_event_type.pkl')
joblib.dump(le_state,      'label_encoder_state.pkl')

print("\n📦 Fichiers sauvegardés !")

# Importance des features
print("\n📈 Importance des features :")
for feat, imp in sorted(zip(FEATURES, model.feature_importances_), key=lambda x: -x[1]):
    bar = '█' * int(imp * 50)
    print(f"   {feat:<22} {bar} {imp:.3f}")

# Test comparatif
print("\n🧪 Tests comparatifs :")
tests = [
    ('Music',      'FESTIVAL',        'Tunis',    20, 7, 5, 4,   0, "Concert gratuit le weekend à Tunis"),
    ('Music',      'FESTIVAL',        'Tunis',    20, 7, 5, 4, 100, "Concert 100DT le weekend à Tunis"),
    ('Music',      'CONCERT',         'Djerba',   21, 7, 6, 3,   0, "Concert gratuit à Djerba en été"),
    ('Music',      'CONCERT',         'Tabarka',  20, 7, 5, 3,   0, "Concert gratuit à Tabarka en été"),
    ('Education',  'WORKSHOP',        'Sfax',      9, 2, 1, 2,  10, "Workshop semaine à Sfax"),
    ('Technology', 'CONFERENCE',      'Tunis',    14, 3, 2, 6,  30, "Conférence Tech à Tunis"),
    ('Adventure',  'OUTDOOR_ACTIVITY','Hammamet', 10, 7, 6, 8,   0, "Activité outdoor à Hammamet"),
    ('Sport',      'SPORTS',          'Sousse',   16, 5, 5, 3,   0, "Event Sport le weekend à Sousse"),
]
for cat, etype, state, hour, month, dow, dur, price, desc in tests:
    row = [[
        le_category.transform([cat])[0],
        le_event_type.transform([etype])[0],
        le_state.transform([state])[0],
        hour, month, dow, dur, price
    ]]
    pred  = model.predict(row)[0]
    label = "low" if pred < 5 else "medium" if pred < 20 else "high" if pred < 50 else "very_high"
    print(f"   {int(pred):>3} participants ({label:<10}) ← {desc}")