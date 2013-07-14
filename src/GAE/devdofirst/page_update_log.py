#!/usr/bin/env python
#
	# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import webapp2
import logging
import json
import dataset_sensor
import re
import string
from google.appengine.ext import db

class ProcessDataTrans:
	def __init__(self, json_data):
		self.json_data = json_data

		device_id = json_data["DeviceId"]
		data_purpose = json_data["DataPurpose"]
		sensor_datas = json_data["sensor_data"]

		for item in sensor_datas:
			splited_data = item.split(",")
			for k in range(len(splited_data)):
				splited_data[k] = splited_data[k].replace("\n", "")

			sDB = dataset_sensor.SensorDataDB()
			sDB.DeviceId = device_id
			sDB.DataPurpose = data_purpose
			sDB.TimeStamp = splited_data[0]
			sDB.TemperatureAmbient = splited_data[1]
			sDB.TemperatureIR = splited_data[2]
			sDB.AccelerometerX = splited_data[3]
			sDB.AccelerometerY = splited_data[4]
			sDB.AccelerometerZ = splited_data[5]
			sDB.BarometricPressure = splited_data[6]
			sDB.Humidity = splited_data[7]
			sDB.GyroX = splited_data[8]
			sDB.GyroY = splited_data[9]
			sDB.GyroZ = splited_data[10]
			sDB.MagnetometerX = splited_data[11]
			sDB.MagnetometerY = splited_data[12]
			sDB.MagnetometerZ = splited_data[13]
			sDB.put()

class Page_Updata_log(webapp2.RequestHandler):
    def get(self):
    	self.response.write("hi update_log page!")

    def post(self):
		getData = self.request.get("sensor_data")
#		self.response.write(getData)
		json_data = json.loads(getData)

		ProcessDataTrans(json_data)

app = webapp2.WSGIApplication([('/update_log', Page_Updata_log)], debug=True)

def main():
    # Set the logging level in the main function
    # See the section on Requests and App Caching for information on how
    # App Engine reuses your request handlers when you specify a main function
	logging.getLogger().setLevel(logging.DEBUG)
	webapp.util.run_wsgi_app(application)

if __name__== "__main__":
	main()
