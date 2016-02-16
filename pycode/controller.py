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

	def setImage(self, image):
		self.image = image

	def setBondlength(self, bondlength):
		self.bondlength = bondlength

	def getDetections(self, image, sigma=False, blocksize=False):
		self.image = image
		segger = DerivativeSegmenter()
		if sigma:
			segger.sigma = sigma
		if blocksize:
			segger.ksize = blocksize
		self.atomD.segmenter = segger
		self.rawdetections = self.atomD.detect(image)

		pointsToSend = {
			'name' 	: "Initial Detections",
			'points': self.rawdetections.tolist()
		}

		pointsets = {}
		pointsets['type'] = 'pointsets'
		pointsets['pointsets'] = [pointsToSend]
		return pointsets

	def constrainDetections(self, image, points, sigma=False, blocksize=False):
		self.image = image
		self.atomD.points = points

		segger = DerivativeSegmenter()
		if sigma:
			segger.sigma = sigma
		if blocksize:
			segger.ksize = blocksize
		self.atomD.segImg = segger.segment(image)

		pointsToSend = {
			'name' 	: "Constrained Detections",
			'points': spatialConstrain(self.atomD, self.bondlength).tolist()
		}

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
