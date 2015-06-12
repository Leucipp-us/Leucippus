import numpy as np
import cv2
from scipy.spatial.distance import cdist

class AdjacencyDetector(object):
# The Current Method for dectecting adjacency will be to
# find the smallest distance from self and then look for
# atoms within that smallest distance * some buffer algo
    def __init__(self, bondlength):
        self.jiggle = 1.3
        self.bondlength = bondlength
        return

    def findBonds(self, atoms):
        dists = cdist(atoms,atoms)
        np.fill_diagonal(dists, np.inf)
        i1 = np.argmin(dists, axis=0)

        newarr = []
        for i in range(0,len(dists)):
            jiggleBound = dists[i] < (dists[i,i1[i]] * self.jiggle)
            bondlengthBound = dists[i] < (1.5 * self.bondlength)
            ind = np.where(jiggleBound & bondlengthBound)[0]
            newarr.append(atoms[ind])
        return np.array(newarr)