import sys
import numpy as np
from TEMAnalysis.AtomDetector import AtomDetector
from TEMAnalysis.AtomicProfiling import AtomProfiler
from TEMAnalysis.AdjacencyDetector import AdjacencyDetector
from TEMAnalysis.Constrainers import spatialConstrain, contourMaximaConstrain

class Controller(object):
	def __init__(self):
		self.image = None
		self.rawdetections = None
		self.bondlength = None
		self.atomD = AtomDetector()
		self.profiler = None
		self.adjD = None
		return

	def setBondlength(self, bondlength):
		self.bondlength = bondlength

	def getDetections(self, image):
		pointsToSend = None
		lpoints = []
		self.image = image

		self.rawdetections = self.atomD.detect(image)

		refinedset = {
			'name' : "Initial Detections",
			'points' : contourMaximaConstrain(image,\
							self.rawdetections,\
							self.atomD.getContours()).tolist()
		}
		pointsToSend = refinedset

		if self.bondlength != None:
			pointsToSend = {
				'name'  : "Constrained Detections",
				'points': self.constrain().tolist()
				}

		pointsets = {}
		pointsets['type'] = 'pointsets'
		pointsets['pointsets'] = [pointsToSend]
		return pointsets

		# what do I want to do here?

	def constrain(self):
		return spatialConstrain(self.atomD, self.bondlength)

	def getFeaturesAndAdmap(self, pointset):
		self.profiler = AtomProfiler(self.bondlength)
		rfeatvec = self.profiler.run(pointset)
		rfeatpts = self.profiler.convertToEuclid(pointset,rfeatvec)
		self.adjD = AdjacencyDetector(self.bondlength)
		admap = self.adjD.findBonds(pointset)

		return [v.tolist() for v in rfeatpts], [v.tolist() for v in admap]