import cv2
import numpy as np
from multiprocessing import Pool

def getBondlengthError(contours):
    from scipy.spatial.distance import cdist
    bl = []
    moments = [cv2.moments(c) for c in contours]
    centroids = np.array([[m['m10']/m['m00'], m['m01']/m['m00']] for m in moments if m['m00'] >= 1.0])
    if len(centroids) == 1 or len(centroids) == 0: return np.inf
    test = cdist(centroids,centroids)
    np.fill_diagonal(test, np.inf)
    i1 = np.argmin(test, axis=0)
    for i in range(0,len(test)):
        ind = np.where((test[i] < test[i,i1[i]] * 1.4))[0]
        for x in ind:
            bl.append(test[i,x])
    bl = np.array(bl)
    return np.linalg.norm(bl - bl.mean()) / np.sqrt(len(bl))

def getError(argtup):
    image, sigma, ksize, morphsize = argtup
    dv = DerivativeSegmenter()
    dv.ksize = ksize
    dv.sigma = sigma
    dv.msize = morphsize
    binImg = dv.segment(image)
    contours, _ = cv2.findContours(binImg.copy(),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
    if len(contours) is 0 or len(contours) is 1: return np.inf
    ble = getBondlengthError(contours)
    return ble if ble > 0 else np.inf

class DerivativeSegmenter(object):
    def __init__(self):
        self.sigma = 0
        self.ksize = 17
        self.msize = 5

    def segment(self, image, bias=0):
        from scipy.ndimage import convolve
        fimage = cv2.GaussianBlur(image,(self.ksize,self.ksize),self.sigma).astype(np.float64)

        lowerbound = fimage < fimage.mean()/2
        def con(k):
            c = convolve(convolve(fimage, k, mode='nearest'),
                         k,
                         mode='nearest')
            c = (c <= (0 + bias)) & ~lowerbound

            return cv2.morphologyEx(c.astype(np.uint8), cv2.MORPH_OPEN, np.ones((self.msize,self.msize),np.uint8))

        c  = con(np.array([[0,0,1],[0,0,0],[-1,0,0]]))
        c &= con(np.array([[0,1,0],[0,0,0],[0,-1,0]]))
        c &= con(np.array([[1,0,0],[0,0,0],[0,0,-1]]))
        c &= con(np.array([[0,0,0],[1,0,-1],[0,0,0]]))
        return c.astype(np.uint8)

class OptiAtomDetector(object):
    def __init__(self):
        self.segger = DerivativeSegmenter()
        self.errfunc = getBondlengthError
        self.segImg = None
        self.contours = None
        self.points = None
        self.image = None

    def detect(self, image):
        self.image = image
        self.segger.ksize, self.segger.sigma, self.segger.msize = self.getOptimalParams(image)
        self.segImg = self.segger.segment(image)
        self.getPoints(self.segImg)
        self.removeSpatially()
        self.fixMergedDetections(self.contours, self.segImg)
        return self.points

    def getPoints(self, binImg):
        contours, _ = cv2.findContours(binImg.copy(),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
        conts = []
        centers = []
        for cnt in contours:
            M = cv2.moments(cnt)
            if M['m00'] >= 1.0:
                centers.append(np.array((int(M['m10']/M['m00']), int(M['m01']/M['m00']))))
                conts.append(cnt)

        self.points = np.array(centers)
        self.contours = np.array(conts)
        return self.points, self.contours

    def getOptimalParams(self, image):

        def ErrorMatrix(image, sigmax=20, kmax=31, mmax=11, debug=False):
            mat = None
            args = [(image, sigma, ksize, msize) for sigma in range(0, sigmax) for ksize in range(5, kmax, 2) for msize in range(3, mmax, 2)]

            if ~debug:
                p = Pool(5)
                mat = np.array([getError(arg) for arg in args]).reshape((sigmax, (kmax-5)/2, (mmax-3)/2))
                p.close(); p.join()
            else:
                mat = np.array(p.map(getError, args)).reshape((sigmax, (kmax-5)/2, (mmax-3)/2))
            return mat


        errMat = ErrorMatrix(image)
        i1, i2, i3 = np.where(errMat == errMat.min())
        return i2[0]*2 + 5, i1[0], i3[0]*2+3

    def getBLmean(self):
        from scipy.spatial.distance import cdist
        bl = []
        D = cdist(self.points, self.points)
        np.fill_diagonal(D, np.inf)
        i1 = np.argmin(D, axis=0)
        for i in range(0,len(D)):
            ind = np.where((D[i] < D[i,i1[i]] * 1.4))[0]
            for x in ind:
                bl.append(D[i,x])
        return np.array(bl).mean()

    def removeSpatially(self, ratio=0.6):
        from scipy.spatial.distance import cdist
        from scipy.spatial import ConvexHull
        points, contours, binImg = self.points, self.contours, self.segImg
        bondlength = self.getBLmean()

        def getViolatingPairs(points):
            distMat = cdist(points, points)
            np.fill_diagonal(distMat,np.inf)
            return np.where(distMat < bondlength * 0.50)

        ir, ic = getViolatingPairs(points)

        ellipses = []
        for c1, c2 in zip(contours[ir], contours[ic]):
            if ir == ic: continue

            M1 = cv2.moments(c1)
            M2 = cv2.moments(c2)
            sc1 = np.squeeze(c1)
            sc2 = np.squeeze(c2)

            ind = min([M1['m00'],M2['m00']])
            if ind == 0:
                if M1['m00'] < ratio * M2['m00']:
                    cv2.fillConvexPoly(binImg, sc1, 0)
                    continue
                elif M2['m00'] < ratio * M1['m00']:
                    continue
            else:
                if M2['m00'] < ratio * M1['m00']:
                    cv2.fillConvexPoly(binImg, sc2, 0)
                    continue
                elif M1['m00'] < ratio * M2['m00']:
                    continue
        points, conts = self.getPoints(binImg)
        contours = np.squeeze(conts)
        ir, ic = getViolatingPairs(points)

        for c1, c2 in zip(contours[ir], contours[ic]):

            M1 = cv2.moments(c1)
            M2 = cv2.moments(c2)
            sc1 = np.squeeze(c1)
            sc2 = np.squeeze(c2)

            cdistMat = cdist(sc1,sc2)
            if np.min(cdistMat) < 10.0:
                nc = np.vstack((sc1,sc2))
                hull = ConvexHull(nc)
                h = nc[hull.vertices]
                cv2.fillConvexPoly(binImg, h, 1)

        self.points, self.contours = self.getPoints(binImg)
        self.segImg = binImg
        return self.points, self.contours, self.segImg

    def fixMergedDetections(self, contours, binImg):
        from scipy.spatial.distance import cdist
        def getArea(c):
            return cv2.contourArea(c)

        def getEcc(c):
            _, axes, _ = cv2.fitEllipse(c)
            MA = max(axes)
            ma = min(axes)
            return np.sqrt(1-(ma/MA)**2)

        def getCMD(c):
            fimage = cv2.GaussianBlur(self.image,(self.segger.ksize,self.segger.ksize),self.segger.sigma).astype(np.float64)
            cen, _, _ = cv2.fitEllipse(c)
            x, y, w, h = cv2.boundingRect(c)
            row,col = np.indices(fimage[y:y+h, x:x+w].shape)
            maxi = fimage[y:y+h, x:x+w].argmax()
            y1,x1= (row.ravel()[maxi], col.ravel()[maxi])
            cen = np.array(cen)
            c1 = np.array((x+x1, y+y1))
            return cdist([cen],[c1])[0,0]

        def getStats(conts, func):
            data = []
            for c in conts:
                data.append(func(c))
            return np.mean(data), np.std(data)

        def rad(d):
            return -(d/180)*np.pi

        nBin = binImg.copy()
        avgArr, stdArr = getStats(contours, getArea)
        avgEcc, stdEcc = getStats(contours, getEcc)
        avgCMD, stdCMD = getStats(contours, getCMD)

        if stdArr < 10 or stdEcc < 0.1 or stdCMD < 1:
            return self.getPoints(nBin);

        for c in contours:
            arrZ = (getArea(c) - avgArr)/stdArr
            eccZ = (getEcc(c) - avgEcc)/stdEcc
            cmdZ = (getCMD(c) - avgCMD)/stdCMD


            cen, axes, theta = cv2.fitEllipse(c)
            cen = tuple([int(round(x)) for x in cen])
            axes = tuple([int(round(x/2)) for x in axes])
            theta = int(round(theta))

            if (arrZ > 1.0) & (eccZ > 1.0) & (cmdZ > 0.0):
                cen, axes, theta = cv2.fitEllipse(c)
                axes = tuple([int(round(x/2)) for x in axes])
                ma = min(axes)

                p1 = (int(cen[0] + ma*np.sin(rad(theta+90))),
                      int(cen[1] + ma*np.cos(rad(theta+90))))
                p2 = (int(cen[0] - ma*np.sin(rad(theta+90))),
                      int(cen[1] - ma*np.cos(rad(theta+90))))
                cv2.line(nBin, p1, p2, (0,0,0), 5)

        cc, co = self.getPoints(nBin)
        self.points, self.contours, self.segImg = cc, co, nBin
        return cc, co, nBin      
