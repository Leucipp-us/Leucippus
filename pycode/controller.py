import sys
import numpy as np
from TEMAnalysis.AtomDetector import AtomDetector
from TEMAnalysis.AtomicProfiling import AtomProfiler
from TEMAnalysis.AdjacencyDetector import AdjacencyDetector
from TEMAnalysis.Constrainers import spatialConstrain, contourMaximaConstrain
from TEMAnalysis.HOI import HOI

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
			'name' 	: "Initial Detections",
			'points': self.rawdetections.tolist()
		}
		pointsToSend = refinedset

		if self.bondlength != None:
			pointsToSend.update({
				'name'  : "Constrained Detections",
				'points': self.constrain().tolist(),
			})


		hois = HOI().run(image, pointsToSend['points'], imagemax=True)
		hois = [a.tolist() for a in hois]
		pointsToSend.update({
			'hois'	: hois
		})

		pointsets = {}
		pointsets['type'] = 'pointsets'
		pointsets['pointsets'] = [pointsToSend]

		return pointsets

	def constrain(self):
		return spatialConstrain(self.atomD, self.bondlength)

	def getFeaturesAndAdmap(self, pointset):
		self.profiler = AtomProfiler(self.bondlength)
		rfeatvec = self.profiler.run(pointset)
		rfeatpts = self.profiler.convertToEuclid(pointset,rfeatvec)
		self.adjD = AdjacencyDetector(self.bondlength)
		admap = self.adjD.findBonds(pointset)

		return [v.tolist() for v in rfeatpts], [v.tolist() for v in admap]

	def getHistogram(self, image, point, blt, imt):
		hois = HOI().run(image, np.array([point]))

		return {
			'type'      : 'histogram',
			'winid'     : 0,
			'histogram' : hois[0].tolist()
		}
