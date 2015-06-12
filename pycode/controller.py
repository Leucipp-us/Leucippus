import sys
import numpy as np
from TEMAnalysis.AtomDetector import AtomDetector
from TEMAnalysis.AtomicProfiling import AtomProfiler
from TEMAnalysis.AdjacencyDetector import AdjacencyDetector

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
		rdetectset = {}
		self.image = image

		self.rawdetections = self.atomD.detect(image)
		rdetectset['points'] = self.rawdetections.tolist()

		if self.bondlength != None:
			self.profiler = AtomProfiler(self.bondlength)
			rfeatvec = self.profiler.run(self.rawdetections)
			rfeatpts = self.profiler.convertToEuclid(self.rawdetections,rfeatvec)
			rdetectset['features'] = [v.tolist() for v in rfeatpts]

			self.adjD = AdjacencyDetector(self.bondlength)
			admap = self.adjD.findBonds(self.rawdetections)
			rdetectset['admap'] = [v.tolist() for v in admap]

		else:
			rdetectset['features'] = None
		lpoints.append(rdetectset)

		if self.bondlength != None:
			self.constrain()


		pointsets = {}
		pointsets['type'] = 'pointsets'
		pointsets['pointsets'] = lpoints
		return pointsets

		# what do I want to do here?

	def constrain(self):
		return