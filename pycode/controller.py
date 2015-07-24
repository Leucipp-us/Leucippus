import sys
import cv2
import tempfile
import numpy as np
import matplotlib.pyplot as plt
from TEMAnalysis.AtomDetector import AtomDetector, DerivativeSegmenter
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

	def getDetections(self, image, sigma=False, blocksize=False):
		pointsToSend = None
		lpoints = []
		self.image = image


		segger = DerivativeSegmenter()
		if sigma:
			segger.sigma = sigma
		if blocksize:
			segger.ksize = blocksize
		self.atomD.setSegmenter(segger)
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

	def getHistogram(self, image, point):
		hois = HOI().run(image, np.array([point]))

		plotImages = []
		with tempfile.NamedTemporaryFile(suffix=".png") as tmpfile:
			for t in hois:
				hoi = t.reshape((3,3))
				f = plt.figure()
				f.set_frameon(False)
				plt.imshow(hoi, interpolation='none',cmap='gray')
				plt.savefig(tmpfile.name)
				plotImage = cv2.imread(tmpfile.name)
				plotImages.append(plotImage.tolist())

		return {
			'type'      : 'histogram',
			'winid'     : 0,
			'histogram' : hois.tolist(),
			'histograms': plotImages
		}
