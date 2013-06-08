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

class ProcessDataTrans:
	def __init__(self, json_sensor_data):
		self.json_sensor_data = json_sensor_data
		logging.debug(self.json_sensor_data)		

class Page_Updata_log(webapp2.RequestHandler):
    def get(self):
    	self.response.write("hi update_log page!")

    def post(self):
		getSensorData = self.request.get("sensor_data")
		self.response.write(getSensorData)
		json_value = json.loads(getSensorData)
		ProcessDataTrans(json_value)

app = webapp2.WSGIApplication([('/update_log', Page_Updata_log)], debug=True)

def main():
    # Set the logging level in the main function
    # See the section on Requests and App Caching for information on how
    # App Engine reuses your request handlers when you specify a main function
	logging.getLogger().setLevel(logging.DEBUG)
	webapp.util.run_wsgi_app(application)

if __name__== "__main__":
	main()
