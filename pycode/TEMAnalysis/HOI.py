import numpy as np

class HOI:
    def __init__(self):
        self.blocksx = 3
        self.blocksy = 3
        self.cellsx = 3
        self.cellsy = 3
        
    def run(self, image, points, imagemax=False):
        maximum = 255
        if imagemax:
            maximum = image.max()
        feats = []
        for p in points:
            featureVector = []
            ystart = p[0] - (self.blocksy * self.cellsy)/2
            xstart = p[1] - (self.blocksx * self.cellsx)/2
            yend = p[0] + (self.blocksy * self.cellsy)/2
            xend = p[1] + (self.blocksx * self.cellsx)/2
            calc = True
            
            
            if xstart < 0 or ystart < 0 or xend >= image.shape[1] or yend >= image.shape[0]:
                calc = False
            
            if calc:
                for i in range(self.blocksx):
                    for j in range(self.blocksy):
                        istart = ystart + i * self.cellsy; iend = istart + self.cellsy
                        jstart = xstart + j * self.cellsx; jend = jstart + self.cellsx
                        
                        blockval = np.mean(image[istart:iend, jstart:jend])
                        featureVector.append(blockval)
                featureVector = np.array(featureVector).reshape((self.blocksx*self.blocksy))
            else:
                featureVector = np.array([-1] * (self.blocksx*self.blocksy))
            feats.append(featureVector)
        return np.array(feats)