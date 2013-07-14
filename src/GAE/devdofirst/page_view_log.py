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
import logging
import json
import dataset_sensor
import re
import string
from google.appengine.ext import db

class Page_View_log(webapp2.RequestHandler):
    def get(self):
		DBGetData = dataset_sensor.SensorDataDB.all()
		DBGetData = db.GqlQuery("SELECT * FROM SensorDataDB ORDER BY TimeStamp")
		logging.debug("=============== DB DATA ==================")
		for data in DBGetData:
			self.response.write("DeviceId=" + data.DeviceId + ",")
			self.response.write("DataPurpose=" + data.DataPurpose + ",")
			self.response.write("TimeStamp=" + data.TimeStamp + ",")
			self.response.write("TemperatureAmbient=" + data.TemperatureAmbient + ",")
			self.response.write("TemperatureIR=" + data.TemperatureIR + ",")
			self.response.write("AccelerometerX=" + data.AccelerometerX + ",")
			self.response.write("AccelerometerY=" + data.AccelerometerY + ",")
			self.response.write("AccelerometerZ=" + data.AccelerometerZ + ",")
			self.response.write("BarometricPressure=" + data.BarometricPressure + ",")
			self.response.write("Humidity=" + data.Humidity + ",")
			self.response.write("GyroX=" + data.GyroX + ",")
			self.response.write("GyroY=" + data.GyroY + ",")
			self.response.write("GyroZ=" + data.GyroZ + ",")
			self.response.write("MagnetometerX=" + data.MagnetometerX + ",")
			self.response.write("MagnetometerY=" + data.MagnetometerY + ",")
			self.response.write("MagnetometerZ=" + data.MagnetometerZ + "")
			self.response.write("<BR>")
#			self.response.write("DeviceId=" + data.DeviceId + " ")
#			self.response.write("DataPurpose=" + data.DataPurpose + " ")
#			self.response.write("TimeStamp=" + data.TimeStamp + " ")
#			self.response.write("TemperatureAmbient=" + data.TemperatureAmbient + " ")
#			self.response.write("TemperatureIR=" + data.TemperatureIR + " ")
#			self.response.write("AccelerometerX=" + data.AccelerometerX + " ")
#			self.response.write("AccelerometerY=" + data.AccelerometerY + " ")
#			self.response.write("AccelerometerZ=" + data.AccelerometerZ + " ")
#			self.response.write("BarometricPressure=" + data.BarometricPressure + " ")
#			self.response.write("Humidity=" + data.Humidity + " ")
#			self.response.write("GyroX=" + data.GyroX + " ")
#			self.response.write("GyroY=" + data.GyroY + " ")
#			self.response.write("GyroZ=" + data.GyroZ + " ")
#			self.response.write("MagnetometerX=" + data.MagnetometerX + " ")
#			self.response.write("MagnetometerY=" + data.MagnetometerY + " ")
#			self.response.write("MagnetometerZ=" + data.MagnetometerZ + " ")
#			self.response.write("<BR>")

app = webapp2.WSGIApplication([('/view_log', Page_View_log)], debug=True)