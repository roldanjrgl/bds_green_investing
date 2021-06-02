import requests
import pandas as pd
import time
from pathlib import Path
url = "https://bing-news-search1.p.rapidapi.com/news/search"

qs = ["$ENPH OR Enphase Energy OR Badri Kothandaraman",
      "$FSLR OR Mark Widmar OR Michael J. Ahearn OR First Solar",
      "$SGRE  OR Siemens Gamesa OR Siemens Wind Power OR Andreas Nauen OR Siemens Energy",
      "$PLUG OR Plug Power Inc OR  Andrew Marsh  OR  George C. McName",
      "$TSLA OR $SCTY OR Solar City OR Tesla OR Lyndon Rive",
      "$RUN OR Sunrun OR Lynn Jurich OR OR Edward Fenster",
      "$SPWR  OR SunPower Corporation OR Thomas H. Werner",
      "$MEL OR $MEZ OR Meridian Energy OR Meridian Energy Limited OR Neal Barclay"
    ]

companies = ["enphase",
             "firstsolar",
             "siemens",
             "plugpower",
             "tesla",
             "sunrun",
             "sunpower",
             "meridian"]
for x in range(100):
    print(x)
    for i in range(len(qs)):
        querystring = {"q":qs[i],"count":'100',"freshness":"Month","textFormat":"Raw","safeSearch":"Off","offset":str(100*x)}
        headers = {
            'x-bingapis-sdk': "true",
            'x-rapidapi-key': "0b4cf39fbdmsh0d53ac7d31b7e45p12c296jsna10b211c1cfd",
            'x-rapidapi-host': "bing-news-search1.p.rapidapi.com"
        }
        response = requests.request("GET", url, headers=headers, params=querystring)
        body = response.json()
        try:
            df = pd.json_normalize(body['value'])
            df = df.sort_index(axis=1)
            file = (companies[i]+".csv")
            df.to_csv(file, mode='a', index = None)
        except KeyError:
            print("Key error")
        time.sleep(.5)

