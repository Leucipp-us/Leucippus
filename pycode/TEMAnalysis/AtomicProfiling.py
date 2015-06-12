import numpy as np
from scipy.spatial.distance import cdist

class AtomProfiler(object):
    def __init__(self, bl):
        self.bondlength = bl
        return

    def setBondLength(self, bl):
        self.bondlength = bl
    
    def run(self, pointset):
        atomDescriptors = []
        for p in pointset:
            localPoints = self.__getLocalPoints(p, pointset)
            descriptor = self.__convertToDescriptor(p, localPoints)
            atomDescriptors.append(descriptor)
        return np.array(atomDescriptors)

    def convertToEuclid(self, points, features):
        featpts = []
        for p, feat in zip(points, features):
            r = feat[:,0]
            theta = feat[:,1]
            x = r * np.cos(theta)
            y = r * np.sin(theta)
            fpts = np.vstack((y,x)).T + p
            featpts.append(fpts)
        return np.array(featpts)

        
    def __getLocalPoints(self, p, pointset):
        p = np.array([p])
        distMat = cdist(pointset, p)
        distMat[distMat == 0.0] = np.inf
        indeces = np.where(distMat < 2*self.bondlength)[0]
        return pointset[indeces]
    
    def __convertToDescriptor(self, p, localPoints):
        localPoints -= p
        x = localPoints[:,0]
        y = localPoints[:,1] 
        r = np.sqrt(x ** 2 + y ** 2)
        theta = np.arctan2(x, y)
        return np.vstack((r,theta)).T