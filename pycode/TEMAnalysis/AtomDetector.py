import numpy as np
import cv2
import matplotlib.pyplot as mpl

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
        return
    
    def segment(self, image, bias=0):
        from scipy.ndimage import convolve
        fimage = cv2.GaussianBlur(image,(17,17),0).astype(np.int32)
        

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
        return c.astype(np.uint8)
        

class AtomDetector(object):
# This class will detect the atoms in the image provided to the detect
# It will come with a default segmenter but you can also provide your
# own.
    def __init__(self):
        self.segmenter = DerivativeSegmenter()
        self.points = None
        self.contours = None
        
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
        contours, _ = cv2.findContours(segImg,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
        self.contours = np.array(contours)
        pointset = []
        for contour in contours:
            p = np.average(contour,axis=0)
            pointset.append(np.round(p))
        self.points = np.squeeze(pointset)
        return self.points