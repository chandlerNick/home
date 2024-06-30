import pandas as pd
import tensorflow as tf
from tensorflow import keras
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import r2_score
import matplotlib.pyplot as plt
import statistics



filepath = "."

# Read in data -- clean it
data = pd.read_csv('merged_data.csv', encoding="ISO-8859-1")
data.dropna(how='any', inplace=True)

# Final data array before splitting rows and columns
data = data.loc[:,["price", "bed", "bath", "acre_lot", "house_size", "Mean", "Stdev"]]

#print(data.head)
#print(list(data.columns))
#print(data.head)

# Split off the response variable
X = data.drop(columns=["price"])
Y = data["price"]

# Split off train data
X_train, X_testdev, y_train, y_testdev = train_test_split(X, Y, test_size=0.2, random_state=42)

# Split remaining data into dev and test
X_test, X_dev, y_test, y_dev = train_test_split(X_testdev, y_testdev, test_size=0.5, random_state=42)

# Scale the data
scaler = StandardScaler()
scaler.fit(X_train)
scaled_X_train = scaler.transform(X_train)
scaled_X_test = scaler.transform(X_test)
scaled_X_dev = scaler.transform(X_dev)


# Define the model architecture
def build_model():
    model = keras.Sequential([
        keras.layers.Dense(512, activation='relu', input_shape=(scaled_X_train.shape[1],)),
        keras.layers.Dense(256, activation='relu'),
        keras.layers.Dense(128, activation='relu'),
        keras.layers.Dense(64, activation='relu'),
        keras.layers.Dense(32, activation='relu'),
        keras.layers.Dense(8, activation='relu'),
        keras.layers.Dense(1)
    ])
    model.compile(optimizer='adam', loss='mean_squared_error', metrics=['mse'])
    return model


# Literature (Wikipedia) says we want models with high variance
# Therefore, we create models with high flexibility so that small changes
# in data yield more significant changes in the model
# the glorot init and SGD should yeild different enough models
# The final predicitons are averaged
def train_models(num_models):
    # List comprehension
    model_list = [build_model() for i in range(num_models)]
    
    # Create the running totals
    running_preds = tf.zeros_like(y_test.shape)
    running_eval_voss = 0
    running_r2 = 0
    r2_list = []
    i = 0
    print("Beginning the ensemble")
    # Go over each model in the list
    for model in model_list:
        history = model.fit(scaled_X_train, y_train, epochs=100, batch_size=512, validation_data=(scaled_X_dev, y_dev), verbose=0)

        # Evaluate the model on the development & test sets
        loss, mse = model.evaluate(scaled_X_dev, y_dev, verbose=0)
        y_pred = model.predict(scaled_X_test)
        r2 = r2_score(y_test, y_pred)

        # Print putput
        print(f'Model number: {i}')
        print(f'Validation set Mean Squared Error: {mse}')
        print(f'Test set R2 score: {r2}')
        
        
        # Update running totals
        running_eval_voss += mse
        running_preds += y_pred
        running_r2 += r2
        r2_list.append(r2)
        i += 1

        # # Plot voss and toss
        # toss = history.history['loss']
        # voss = history.history['val_loss']
        # eps = range(1, len(toss) + 1)

        # plt.figure(figsize=(10, 6))
        # plt.plot(eps, toss, 'bo-', label='Training Loss')
        # plt.plot(eps, voss, 'ro-', label='Validation Loss')
        # plt.title('Training and Validation Metrics')
        # plt.xlabel('Epochs')
        # plt.ylabel('Metrics')
        # plt.legend()
        # plt.grid(True)
        

    # Average Metrics
    avg_eval_voss = running_eval_voss/num_models
    avg_r2 = running_r2/num_models
    print(f'Average Validation MSE: {avg_eval_voss}')
    print(f'Average R-Squared: {avg_r2}')

    # Ensembled Predictions and Evaluation
    avg_pred = running_preds/num_models
    ensemble_r2 = r2_score(y_test, avg_pred)
    print(f'Ensembled R-Squared (Arithmetic Mean): {ensemble_r2}')
    print(f'Ensembled R-Squared (Median): {statistics.median(r2_list)}')

# Ensemble 5 models
train_models(10)
