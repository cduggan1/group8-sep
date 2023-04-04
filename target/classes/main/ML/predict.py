import sys
import pickle
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize

# Load the trained model from the file


stop_words = set(stopwords.words('english'))

# Get input from the command line argument
input_text = sys.argv[1]
working_dir = sys.argv[2]

with open(working_dir+'/model.pkl', 'rb') as f:
    vectorizer, clf = pickle.load(f)

# Preprocess the input text and vectorize it
input_text = ' '.join([word for word in word_tokenize(input_text) if word.lower() not in stop_words])
X_test = vectorizer.transform([input_text]).toarray()

# Predict the output using the trained model
output = clf.predict(X_test)[0]
confidence = clf.predict_proba(X_test).max()

# Print the output and confidence
print(output, confidence)
