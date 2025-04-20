#! /usr/bin/env python3

from urllib.request import Request, urlopen
import xml.etree.ElementTree as ET
from requests import post
from datetime import datetime, timedelta
import os
from os.path import join


URL = 'http://www.efa-bw.de/trias'
HEADERS_REQUESTS = {'Content-Type': 'text/xml', 'User-Agent': 'Python-urllib/3.10'}
HEADERS_URLLIB = {'Content-Type': 'text/xml'}

filename = join(os.environ["HOME"], "chaquopy/AssetFinder/app/triasReqToUni.xml")


def letsDoIt():
    treeReq = ET.parse(filename)
    rootReq = treeReq.getroot()
    
    depTime = rootReq[0][2][0][0][1]
    reqCurrTime = datetime.utcnow()
    depTime.text = reqCurrTime.strftime('%Y-%m-%dT%H:%M:%SZ')
    treeReq.write(filename)
    #print("Request Updated with current UTC time: ",depTime.text)

    with open(filename, 'rb') as file:
        xml = file.read()

    response = post(URL, data=xml, headers=HEADERS_REQUESTS)
    # print(response.status_code, response.text, len(xml))
    #print(response.status_code, len(xml))
    # print(type(response.text))
    request = Request(URL, data=xml, headers=HEADERS_URLLIB)
    
    #print("---------------")
    root = ET.fromstring(response.text)
    startTime = root[0][5][0][2][1][6][1][0][2][0]
    #print("Next departure at: ",startTime.text)
    given_datetime = datetime.strptime(startTime.text, '%Y-%m-%dT%H:%M:%SZ') + timedelta(hours=2)
    # Get the current datetime
    current_datetime = datetime.utcnow() + timedelta(hours=2)
    # print("Current time: ", current_datetime)

    estimatedDepTime = given_datetime
    
    ## **** NEW STUFF FOR ESTIMATED TIME
    try:
        estimatedTimeWithDelay = root[0][5][0][2][1][6][1][0][2][1]
        estimatedDepTime = datetime.strptime(estimatedTimeWithDelay.text, '%Y-%m-%dT%H:%M:%SZ') + timedelta(hours=2)
        delayInMinutes = (estimatedDepTime - given_datetime).total_seconds() / 60
    except IndexError:
        delayInMinutes = 0.0
    ## ***** END
    
    # Calculate the time difference in minutes
    time_difference = (estimatedDepTime - current_datetime).total_seconds() / 60
    
    # Round the float number to 2 decimal points
    rounded_number = round(time_difference, 2)

    # Convert the rounded number to a string with 2 decimal points
    rounded_number_str = "{:.2f}".format(rounded_number)
    depTimeStr = datetime.strftime(given_datetime,'%Y-%m-%dT%H:%M:%SZ')
    delayInStr = "{:.2f}".format(delayInMinutes)
    return rounded_number_str + "*" + depTimeStr[11:]+"*"+delayInStr

