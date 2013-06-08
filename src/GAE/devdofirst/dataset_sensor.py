#!/usr/bin/env python

from google.appengine.ext import db

class SensorDataDB(db.Model):
	DeviceId = db.StringProperty()
	DataPurpose = db.StringProperty()
	TimeStamp = db.StringProperty()
	TemperatureAmbient = db.StringProperty()
	TemperatureIR = db.StringProperty()
	AccelerometerX = db.StringProperty()
	AccelerometerY = db.StringProperty()
	AccelerometerZ = db.StringProperty()
	BarometricPressure = db.StringProperty()
	Humidity = db.StringProperty()
	GyroX = db.StringProperty()
	GyroY = db.StringProperty()
	GyroZ = db.StringProperty()
	MagnetometerX = db.StringProperty()
	MagnetometerY = db.StringProperty()
	MagnetometerZ = db.StringProperty()
