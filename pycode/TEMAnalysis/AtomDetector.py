import numpy as np
import numpy.linalg as la
import cv2
import matplotlib.pyplot as mpl
import math
import copy
import sys

class DerivativeSegmenter(object):
    """ This class contains the segmentation method used in our atomic detection
        algorithm.
        The DerivativeSegmenter segments images based on their second derivate
        in many directions and combines the results from each direction to get
        the final segmentation image.
    """
    def __init__(self):
        self.sigma = 0
        self.ksize = 17
        return

    def segment(self, image, bias=0):
        from scipy.ndimage import convolve
        fimage = cv2.GaussianBlur(image,(self.ksize,self.ksize),self.sigma).astype(np.float64)

        def con(k):
            c = convolve(convolve(fimage, k, mode='nearest'),
                         k,
                         mode='nearest')
            c = (c <= (0 + bias))
            return cv2.morphologyEx(c.astype(np.uint8), cv2.MORPH_OPEN, np.ones((5,5),np.uint8))

        c  = con(np.array([[0,0,1],[0,0,0],[-1,0,0]]))
        c &= con(np.array([[0,1,0],[0,0,0],[0,-1,0]]))
        c &= con(np.array([[1,0,0],[0,0,0],[0,0,-1]]))
        c &= con(np.array([[0,0,0],[1,0,-1],[0,0,0]]))
        return c.astype(np.uint8)

class AtomDetector(object):
# This class will detect the atoms in the image provided to the detect
# It will come with a default segmenter but you can also provide your
# own.
    def __init__(self):
        self.segmenter = DerivativeSegmenter()
        self.points = None
        self.contours = None
        self.segImg = None

    def detect(self, image):
        return self.__grabAtoms(image)

    def __grabAtoms(self, image):
        from scipy.spatial import ConvexHull

        segImg = self.segmenter.segment(image)
        contours, _ = cv2.findContours(segImg.copy(),
                                        cv2.RETR_EXTERNAL,
                                        cv2.CHAIN_APPROX_NONE)

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
