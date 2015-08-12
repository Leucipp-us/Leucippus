import numpy as np
import numpy.linalg as la
import cv2
import matplotlib.pyplot as mpl
import math
import copy
import sys

class Segmenter(object):
# This class will be the default segmenter for the atom detector
# class. This class takes the thresholding values to be 3/4 * maxval
    def __init__(self):
        return
    
    def segment(self, image):
        a, maxVal, b, c = cv2.minMaxLoc(image)
        ret, im2 = cv2.threshold(image, maxVal*3/4 ,255, cv2.THRESH_BINARY)
        return im2

class AdaptiveSegmenter(object):
    def __init__(self, pixelrat):
        self.pixelrat = pixelrat
        return
    
    def segment(self, image, bias=0):
        if self.pixelrat < 0:
            return None
        else:
            N = self.pixelrat/3
            if N % 2 == 0:
                N-=1
            return cv2.adaptiveThreshold(image,255,cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, N, bias)

class DerivativeSegmenter(object):
    def __init__(self):
        self.sigma = 0
        self.ksize = 17
        return
    
    def segment(self, image, bias=0):
        from scipy.ndimage import convolve
        fimage = cv2.GaussianBlur(image,
                                  (self.ksize,self.ksize),
                                  self.sigma).astype(np.int32)
        

        def con(k):
            c = convolve(convolve(fimage, k, mode='constant', cval=0),
                         k, 
                         mode='constant',
                         cval=0) < (0 + bias)
            return cv2.morphologyEx(c.astype(np.uint8), cv2.MORPH_OPEN, np.ones((5,5),np.uint8))

        c  = con(np.array([[0,0,1],[0,0,0],[-1,0,0]]))
        c &= con(np.array([[0,1,0],[0,0,0],[0,-1,0]]))
        c &= con(np.array([[1,0,0],[0,0,0],[0,0,-1]]))
        c &= con(np.array([[0,0,0],[1,0,-1],[0,0,0]]))
        c &= con(np.array([[0,0,-1],[0,0,0],[1,0,0]]))
        c &= con(np.array([[0,-1,0],[0,0,0],[0,1,0]]))
        c &= con(np.array([[-1,0,0],[0,0,0],[0,0,1]]))
        c &= con(np.array([[0,0,0],[-1,0,1],[0,0,0]]))
        c[c==1] = 255
        return cv2.morphologyEx(c.astype(np.uint8), cv2.MORPH_DILATE, np.ones((3,3), np.uint8))
        

class AtomDetector(object):
# This class will detect the atoms in the image provided to the detect
# It will come with a default segmenter but you can also provide your
# own.
    def __init__(self):
        self.segmenter = DerivativeSegmenter()
        self.points = None
        self.contours = None
        self.segImg = None
        
    def getSegmenter(self):
        return self.segmenter
        
    def getContours(self):
        return self.contours

    def getPoints(self):
        return self.points
        
    def setSegmenter(self, seg):
        self.segmenter = seg
    
    def detect(self, image):
        return self.__grabAtoms(self.__segment(image))

    def __segment(self, image):
        return self.segmenter.segment(image)
    
    def __grabAtoms(self, segImg):
        from scipy.spatial import ConvexHull
        contours, _ = cv2.findContours(segImg.copy(),
                                        cv2.RETR_EXTERNAL,
                                        cv2.CHAIN_APPROX_NONE)
        cv2.imshow('seg2',segImg)
        cv2.waitKey(500)

        for cnt in contours:
            M = cv2.moments(cnt)
            if M['m00'] > 0.0:
                c = np.squeeze(cnt)
                cv2.fillConvexPoly(segImg, c[ConvexHull(c).vertices], 255)
            else:
                cv2.fillConvexPoly(segImg, cnt, 0)

        contours, _ = cv2.findContours(segImg.copy(),
                                        cv2.RETR_EXTERNAL,
                                        cv2.CHAIN_APPROX_NONE)        

        conts = []
        centers = []
        for cnt in contours:
            M = cv2.moments(cnt)
            if M['m00'] > 0.0:
                centers.append(np.array((int(M['m10']/M['m00']), int(M['m01']/M['m00']))))
                conts.append(cnt)

        self.segImg = segImg
        self.points = np.array(centers)
        self.contours = np.array(conts)
        return self.points

    def __invertBinary(self, binimg):
        ones = binimg == 255
        zeros  = binimg == 0
        retimg = binimg.copy()
        retimg[zeros] = 255
        retimg[ones] = 0
        return retimg

