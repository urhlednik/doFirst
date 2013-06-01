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

class Page_Changgun(webapp2.RequestHandler):
    def get(self):
    	self.response.write("{\"sensor_data\": [\"13. 6. 1. 18:01:32,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000\n\",\"13. 6. 1. 18:01:33,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000\",\"13. 6. 1. 18:01:34,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000\",\"13. 6. 1. 18:01:35,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000\",\"13. 6. 1. 18:01:36,28.500,29.151,0.094,-0.328,0.812,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000\",\"13. 6. 1. 18:01:37,28.500,29.437,-0.125,0.016,0.875,1003.000,37.774,0.000,0.000,0.000,52.063,-35.736,51.575\",\"13. 6. 1. 18:01:38,28.500,31.174,-0.031,-0.062,1.031,1003.000,38.080,-0.061,0.275,0.259,51.941,-36.041,51.422\",\"13. 6. 1. 18:01:39,28.500,28.821,-0.016,-0.047,1.016,1003.000,38.453,-0.061,0.275,0.259,52.185,-35.950,51.819\",\"13. 6. 1. 18:01:40,28.500,28.976,-0.016,-0.078,1.016,1003.000,38.682,-0.244,0.374,0.259,52.216,-35.919,51.849\"],\"DeviceId\": \"e59903226c3e1c21c5700313e44b54e4\",\"DataPurpose\": \"test\"}")

app = webapp2.WSGIApplication([('/changgun', Page_Changgun)], debug=True)