from AtomDetector import DerivativeSegmenter
import cv2
import numpy as np
from scipy.spatial.distance import cdist

def detect_local_maxima(arr):
    from scipy.ndimage.filters import maximum_filter
    from scipy.ndimage.morphology import generate_binary_structure, binary_erosion
    # http://stackoverflow.com/questions/3684484/peak-detection-in-a-2d-array/3689710#3689710
    """
    Takes an array and detects the troughs using the local maximum filter.
    Returns a boolean mask of the troughs (i.e. 1 when
    the pixel's value is the ne            print pointsToAdd
        print pointsToAdd.shapeighborhood maximum, 0 otherwise)
    """
    def clusterMaxima(maxima, imageshape,):
        pts = []
        contours, _ = cv2.findContours(maxima.astype(np.uint8),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
        for contour in contours:
            pts.append(np.round(np.average(contour,axis=0)))
        return np.squeeze(pts).astype(int)
    
    arr = cv2.GaussianBlur(arr,(17,17),0)
    neighborhood = generate_binary_structure(len(arr.shape),2)
    local_max = (maximum_filter(arr, footprint=neighborhood)==arr)
    background = (arr<10)
    eroded_background = binary_erosion(background, structure=neighborhood, border_value=1)
    detected_maxima = local_max & np.logical_not(eroded_background)
    return clusterMaxima(detected_maxima, (arr.shape[0],arr.shape[1]))
    
def maximaFilter(pointset, maxima):
    #compare the local maxima to the pointset
    #any points that do not match a local maxima flag them
    #return the flagged points
    distMat = cdist(maxima,pointset)
    i1 = np.argmin(distMat, axis=0)
    i2 = np.arange(0,distMat.shape[1])
    localMaxViolators = np.where(distMat[i1,i2] > (0.25 * 10.0))
    return localMaxViolators[0]

def contourFilter(image, pointset, contours, maxima):
        seg = DerivativeSegmenter()
        
        maximaViolators = maximaFilter(pointset, maxima)
        
        pointsToAdd = []
        pointsToRemove = np.zeros(len(maximaViolators), dtype=np.bool)
        rPConts = contours[maximaViolators]
        
        def contourContains(contour, points):
            count = 0
            for p in points:
                if cv2.pointPolygonTest(contour,(int(p[0]),int(p[1])),False) > -1:
                    count+=1
            return count
        
        if len(rPConts) == 0:
            return pointset
        
        for i in range(0,len(rPConts)):
            maximaInContour = contourContains(rPConts[i],maxima)
            
            if maximaInContour > 1:
                center = np.round(np.mean(rPConts[i],axis=0)[0]).astype(int)
                blocksize = (2*10)#2 angstroms times ratio
                
                a0min = center[0] - blocksize/2
                a0max = center[0] + blocksize/2
                a1min = center[1] - blocksize/2
                a1max = center[1] + blocksize/2
        
                roiImg = image[a1min:a1max, a0min:a0max]
                offset = np.array([[a0min,a1min]])
                
                bias = 0
                while True:
                    bias-=1
                    img = seg.segment(roiImg, bias=bias)
                    contours, _ = cv2.findContours(img.copy(),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
            
                    count = 0; solution = True
                    for contour in contours:
                        r =  cv2.boundingRect(contour)
                        if r[2] == 1 or r[3] == 1:
                            continue
                        
                        count += 1
                        maximaInContours = contourContains(contour+offset,maxima)                    
                        if maximaInContours != 1:
                            solution = False
                            break
                    
                    if count == 0:
                        break
                    
                    if solution:
                        pointsToRemove[i] = True
                        for contour in contours:
                            r =  cv2.boundingRect(contour)
                            if r[2] == 1 or r[3] == 1:
                                continue
                            pointsToAdd.append(np.round(np.average(contour,axis=0) + offset))
                        break
                        
        rmS = np.zeros(len(pointset), dtype=np.bool)
        rmS[maximaViolators[pointsToRemove]] = True
        filteredPointset = pointset[~rmS]
        
        if len(pointsToAdd) > 0:
            pointsToAdd = np.squeeze(pointsToAdd)
        
            
            if len(pointsToAdd.shape)  == 1:
                pointsToAdd = np.array([pointsToAdd])
                
            filteredPointset = np.vstack((filteredPointset, pointsToAdd))
        
        return filteredPointset

# def spatialConstrain(image, pointset, bondlength):
#     maxima = detect_local_maxima(image)
#     spatialViolators = spatialFilter(pointset, maxima, bondlength)
    
#     #remove spatial violators
#     rmS = np.zeros(len(pointset), dtype=np.bool)
#     rmS[spatialViolators] = True
#     return ~rmS

def spatialConstrain(atomD, bondlength):
    return removeSpatialOffenders(atomD, bondlength)

def removeSpatialOffenders(atomD, bondlength):
    from scipy.spatial import ConvexHull
    def getViolatingPairs(points):
        distMat = cdist(points, points)
        np.fill_diagonal(distMat,np.inf)
        return np.where(distMat < bondlength * 0.50)

    binImg = atomD.segImg
    contours = atomD.getContours()
    points = atomD.getPoints()
    
    ir, ic = getViolatingPairs(points)
    ellipses = []
    for c1, c2 in zip(contours[ir], contours[ic]):
        
        M1 = cv2.moments(c1)
        M2 = cv2.moments(c2)
        sc1 = np.squeeze(c1)
        sc2 = np.squeeze(c2)
        
        ind = min([M1['m00'],M2['m00']])
        if ind == 0:
            if M1['m00'] < 0.35 * M2['m00']:
                cv2.fillConvexPoly(binImg, sc1, 0)
                continue
            elif M2['m00'] < 0.35 * M1['m00']:
                continue
        else:
            if M2['m00'] < 0.35 * M1['m00']:
                cv2.fillConvexPoly(binImg, sc2, 0)
                continue
            elif M1['m00'] < 0.35 * M2['m00']:
                continue
                
    points, conts, binImg = grabPointsAndContours(binImg)
    contours = np.squeeze(conts)
    ir, ic = getViolatingPairs(points)
    
    for c1, c2 in zip(contours[ir], contours[ic]):
        
        M1 = cv2.moments(c1)
        M2 = cv2.moments(c2)
        sc1 = np.squeeze(c1)
        sc2 = np.squeeze(c2)
        
        cdistMat = cdist(sc1,sc2)
        if np.min(cdistMat) < 5.0:
            nc = np.vstack((sc1,sc2))
            hull = ConvexHull(nc)
            h = nc[hull.vertices]
            cv2.fillConvexPoly(binImg, h, 1)
    
    centers, conts, binImg = grabPointsAndContours(binImg)
    atomD.segImg = binImg
    atomD.contours = conts
    atomD.points = centers 
    return centers

def contourMaximaConstrain(image, points, contours):
	maxima = detect_local_maxima(image)
	return contourFilter(image, points, contours, maxima)

def grabPointsAndContours(segImg):
    from scipy.spatial import ConvexHull
    contours, _ = cv2.findContours(segImg.copy(),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
    
    for cnt in contours:
        M = cv2.moments(cnt)
        if M['m00'] >= 1.0:
            c = np.squeeze(cnt)
            cv2.fillConvexPoly(segImg, c[ConvexHull(c).vertices], 1)
        else:
            cv2.fillConvexPoly(segImg, cnt, 0)

    contours, _ = cv2.findContours(segImg.copy(),cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)        
    
    conts = []
    centers = []
    for cnt in contours:
        M = cv2.moments(cnt)
        if M['m00'] >= 1.0:
            centers.append(np.array((int(M['m10']/M['m00']), int(M['m01']/M['m00']))))
            conts.append(cnt)
        
    centers = np.array(centers)
    return centers, conts, segImg