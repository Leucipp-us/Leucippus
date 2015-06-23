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
		lpoints = []
		self.image = image

		self.rawdetections = self.atomD.detect(image)
		rdetectset = {
			'name'  : "Raw Detections",
			'points': self.rawdetections.tolist()
		}
		lpoints.append(rdetectset)

		refinedset = {
			'name' : "Refined Detections",
			'points' : contourMaximaConstrain(image,\
							self.rawdetections,\
							self.atomD.getContours()).tolist()
		}
		lpoints.append(refinedset)

		if self.bondlength != None:
			# rdetectset['features'],\
			# rdetectset['admap'] = self.getFeaturesAndAdmap(self.rawdetections)

			constrainset = {}
			constrainset['name'] = "Constrained Detections"
			cpoints = self.constrain(image, self.rawdetections)
			constrainset['points'] = cpoints.tolist()
			# constrainset['features'],\
			# constrainset['admap'] = self.getFeaturesAndAdmap(cpoints)
			lpoints.append(constrainset)

		pointsets = {}
		pointsets['type'] = 'pointsets'
		pointsets['pointsets'] = lpoints
		return pointsets

		# what do I want to do here?

	def constrain(self, image, pointset):
		return pointset[spatialConstrain(image, pointset, self.bondlength)]

	def getFeaturesAndAdmap(self, pointset):
		self.profiler = AtomProfiler(self.bondlength)
		rfeatvec = self.profiler.run(pointset)
		rfeatpts = self.profiler.convertToEuclid(pointset,rfeatvec)
		self.adjD = AdjacencyDetector(self.bondlength)
		admap = self.adjD.findBonds(pointset)

		return [v.tolist() for v in rfeatpts], [v.tolist() for v in admap]