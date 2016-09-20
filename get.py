import requests, json, sys

url = "http://"+sys.argv[1]+"/Thingworx/Things/"+sys.argv[2]+"/Properties/"+sys.argv[3]
headers = {'Accept':'application/json'}
auth = (sys.argv[4], sys.argv[5])
response = requests.get(url=url, headers=headers, auth=auth)
json_data = json.loads(response.text)
print(json_data["rows"][0][sys.argv[3]])
