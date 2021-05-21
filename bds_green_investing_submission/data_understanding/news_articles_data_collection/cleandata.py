import requests
import pandas as pd
import time
import nltk
##nltk.download()
from nltk.sentiment import SentimentIntensityAnalyzer
import string
import re
from pathlib import Path
companies = ["enphase",
             "firstsolar",
             "siemens",
             "plugpower",
             "tesla",
             "sunrun",
             "sunpower",
             "meridian"]


def remove_punct(text):
##    print(text)
    text  = "".join([char for char in text if char not in string.punctuation])
    text = re.sub('[0-9]+', '', text)
    return text

def tokenization(text):
    text = re.split('\W+', text)
    return text

def remove_stopwords(text):
    text = [word for word in text if word not in stopword]
    return text

def stemming(text):
    text = [ps.stem(word) for word in text]
    return text

def lemmatizer(text):
    text = [wn.lemmatize(word) for word in text]
    return text


for i in range(len(companies)):
    print(i)
    file = (companies[i]+"_cleaned.csv")
    df = pd.read_csv(file)
    print(df.head())
    print(df.shape)
    print(df.columns)

    # combine article name and description in 1 cell
    df["combined"] = df["name"] + df["description"]

    # tokenization
    df["_punct"] = df["combined"].apply(lambda x: remove_punct(x))

    #remove
    df['_tokenized'] = df['_punct'].apply(lambda x: tokenization(x.lower()))

    # remove stopwords
    stopword = nltk.corpus.stopwords.words('english')
    df['_nonstop'] = df['_tokenized'].apply(lambda x: remove_stopwords(x))

    # stemming and lammitization
    ps = nltk.PorterStemmer()
    df['_stemmed'] = df['_nonstop'].apply(lambda x: stemming(x))
    wn = nltk.WordNetLemmatizer()
    df['_lemmatized'] = df['_nonstop'].apply(lambda x: lemmatizer(x))

##    print(df.dtypes)
##    print((df['_lemmatized']))
    # get sentiment
    sia = SentimentIntensityAnalyzer()
    df['sentiment'] = df['_lemmatized'].apply(lambda x: sia.polarity_scores(' '.join(x))['compound'])

##    df['dates'] = df['datePublished'].apply(lambda x: x[0:4]+x[5:7]+x[8:10])
    df['dates'] = df['datePublished'].apply(lambda x: x[:10])
##    print(df['dates'])
    df['dates'] = pd.to_datetime(df['dates'], format='%Y-%m-%d')
    
    series = df.resample('W', on='dates').mean()
    
    
    newfile = (companies[i]+"_datacleaned"+".csv")
    df.to_csv(newfile, index = None)
    newseries = (companies[i]+"_series"+".csv")
    series.to_csv(newseries, index = True)
