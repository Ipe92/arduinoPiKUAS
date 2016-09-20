import requests, json, sys

url = "http://"+sys.argv[1]+"/Thingworx/Things/"+sys.argv[2]+"/Properties/"+sys.argv[3]
headers = {'Content-Type':'application/json'}
json_data = {sys.argv[3]:sys.argv[4]=="True"}
auth = (sys.argv[5], sys.argv[6])

response = requests.put(url=url, headers=headers, json=json_data, auth=auth)

print("Now, "+sys.argv[2]+"."+sys.argv[3]+" is "+str(sys.argv[4]=="True"))
