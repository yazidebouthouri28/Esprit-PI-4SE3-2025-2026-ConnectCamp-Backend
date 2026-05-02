import pandas as pd
import numpy as np
import os
import json
from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)

# Load models and encoders
model = joblib.load("event_popularity_model.pkl")
le_category = joblib.load("label_encoder_category.pkl")
le_event_type = joblib.load("label_encoder_event_type.pkl")
le_state = joblib.load("label_encoder_state.pkl")

DATASET_PATH = "training_data.csv"

def load_or_create_dataset():
    if os.path.exists(DATASET_PATH):
        return pd.read_csv(DATASET_PATH)
    else:
        return pd.DataFrame(columns=[
            'category', 'event_type', 'state', 
            'hour', 'month', 'day_of_week', 
            'duration_hours', 'price', 'actual_attendees'
        ])

@app.route("/health", methods=["GET"])
def health():
    return "OK", 200

@app.route("/predict", methods=["POST"])
def predict():
    data = request.json
    try:
        cat = str(data.get("category", "Other")).strip()
        type_val = str(data.get("event_type", "Other")).strip()
        state = str(data.get("state", "Unknown")).strip()

        # Handle unknown categories safely
        if cat not in le_category.classes_: cat = le_category.classes_[0]
        if type_val not in le_event_type.classes_: type_val = le_event_type.classes_[0]
        if state not in le_state.classes_: state = le_state.classes_[0]

        cat_enc = le_category.transform([cat])[0]
        type_enc = le_event_type.transform([type_val])[0]
        state_enc = le_state.transform([state])[0]
        
        hour = int(data.get("hour", 12))
        month = int(data.get("month", 6))
        dow = int(data.get("day_of_week", 2))
        dur = int(data.get("duration_hours", 2))
        price = float(data.get("price", 0.0))
        
        X = pd.DataFrame([{
            'category_encoded': cat_enc, 
            'event_type_encoded': type_enc, 
            'state_encoded': state_enc, 
            'hour': hour, 
            'month': month, 
            'day_of_week': dow, 
            'duration_hours': dur, 
            'price': price
        }])
        
        pred = model.predict(X)[0]
        
        predicted = int(max(0, pred))

        # Post-processing: Price elasticity correction
        # Tree-based models can't extrapolate beyond training price ranges,
        # so we apply a realistic economic decay for expensive events.
        # Free events (price=0) get no penalty. As price increases beyond
        # the training range (~150 DT), attendance decays smoothly.
        if price > 0:
            # Sigmoid-style decay: factor = 1 / (1 + (price/150)^1.5)
            # price=0 -> factor=1.0, price=150 -> factor=0.35,
            # price=500 -> factor=0.08, price=1M -> ~0
            price_factor = 1.0 / (1.0 + (price / 150.0) ** 1.5)
            predicted = int(max(0, predicted * price_factor))

        if predicted < 5: popularity = "low"
        elif predicted < 20: popularity = "medium"
        elif predicted < 50: popularity = "high"
        else: popularity = "very_high"
        
        badge = "Explorer"
        if popularity == "medium": badge = "Connector"
        elif popularity == "high": badge = "Networker"
        elif popularity == "very_high": badge = "Community Leader"
        
        return jsonify({
            "predicted_attendees": predicted,
            "popularity": popularity,
            "badge_suggestion": badge
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 400

@app.route("/retrain", methods=["POST"])
def retrain():
    data = request.json
    print("-- Nouvelle donnee recue pour reentrainement:", data)

    try:
        df = load_or_create_dataset()

        category = str(data.get("category", "Other")).strip()
        event_type = str(data.get("event_type", "Other")).strip()
        state = str(data.get("state", "Unknown")).strip()

        if category not in le_category.classes_:
            le_category.classes_ = np.append(le_category.classes_, category)
        if event_type not in le_event_type.classes_:
            le_event_type.classes_ = np.append(le_event_type.classes_, event_type)
        if state not in le_state.classes_:
            le_state.classes_ = np.append(le_state.classes_, state)

        new_row = {
            'category': category,
            'event_type': event_type,
            'state': state,
            'hour': int(data.get("hour", 12)),
            'month': int(data.get("month", 6)),
            'day_of_week': int(data.get("day_of_week", 2)),
            'duration_hours': int(data.get("duration_hours", 2)),
            'price': float(data.get("price", 0)),
            'actual_attendees': int(data.get("actual_attendees", 0))
        }
        df = pd.concat([df, pd.DataFrame([new_row])], ignore_index=True)
        df.to_csv(DATASET_PATH, index=False)
        print(f"OK Dataset mis a jour: {len(df)} lignes")

        if len(df) >= 10:
            _retrain_model(df)
            return jsonify({
                "status": "retrained",
                "dataset_size": len(df),
                "message": f"Modèle réentraîné avec {len(df)} événements"
            })
        else:
            return jsonify({
                "status": "data_saved",
                "dataset_size": len(df),
                "message": f"Données sauvegardées ({len(df)}/10 minimum pour réentraîner)"
            })

    except Exception as e:
        print("ERROR retrain:", repr(e))
        return jsonify({"error": str(e)}), 400


def _retrain_model(df):
    global model, le_category, le_event_type, le_state

    df['category_encoded']   = le_category.transform(df['category'])
    df['event_type_encoded'] = le_event_type.transform(df['event_type'])
    df['state_encoded']      = le_state.transform(df['state'])

    X = df[['category_encoded', 'event_type_encoded', 'state_encoded',
            'hour', 'month', 'day_of_week', 'duration_hours', 'price']]
    y = df['actual_attendees']

    model.fit(X, y)

    joblib.dump(model,         "event_popularity_model.pkl")
    joblib.dump(le_category,   "label_encoder_category.pkl")
    joblib.dump(le_event_type, "label_encoder_event_type.pkl")
    joblib.dump(le_state,      "label_encoder_state.pkl")

    print("OK Modele reentraine et sauvegarde !")


@app.route("/dataset/stats", methods=["GET"])
def dataset_stats():
    df = load_or_create_dataset()
    return jsonify({
        "total_events": len(df),
        "categories": df['category'].value_counts().to_dict() if len(df) > 0 else {},
        "avg_attendees": round(df['actual_attendees'].mean(), 1) if len(df) > 0 else 0
    })

if __name__ == "__main__":
    app.run(port=5000, host="0.0.0.0", debug=True)
