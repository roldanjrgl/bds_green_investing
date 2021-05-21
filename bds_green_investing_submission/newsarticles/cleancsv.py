import requests
import pandas as pd
import time
from pathlib import Path
companies = ["enphase",
             "firstsolar",
             "siemens",
             "plugpower",
             "tesla",
             "sunrun",
             "sunpower",
             "meridian"]

for i in range(len(companies)):
    print(i)
    file = (companies[i]+"_new.csv")
    df = pd.read_csv(file)
    df = df.drop_duplicates(subset=['description'])
    newfile = (companies[i]+"_cleaned"+".csv")
    df.to_csv(newfile, index = None)
