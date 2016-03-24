import networkx as nx
import numpy as np
import cv2

class Graph:
    def __init__(self):
        self.points = None
        self.graph = None
        self.cycles = None

    def findNeighbourhood(self, image, points, ksize, sigma):
        self.points, self.graph = self.getBonds(points, image, ksize, sigma)
        self.cycles = self.find_cycles(image, self.graph, self.points)
        return self.graph, self.cycles

    def getBonds(self, points, image, ksize, sigma):
        from scipy.spatial.distance import cdist
        def getInitial(atoms):
            """Gets the initial set of detections using a radius based on the closest atom"""
            D = cdist(atoms,atoms)
            np.fill_diagonal(D, np.inf)
            i1 = np.argmin(D, axis=0)

            g = nx.Graph()
            for i in range(0,len(D)):
                ind = np.where((D[i] < D[i,i1[i]] * 1.6))[0]
                for x in ind:
                    g.add_edge(i,x, weight=D[i,x])
            return g

        def stageOnePrune(atoms, Graph):
            """Generates an image mask which specifies 'areas of charge'.
            If a line is not entirely within this areas it is removed"""
            chargeratio=0.80 #magic number that gives us just enough room to prune most incorrect lines
            lineImg = np.zeros(image.shape, dtype=np.uint8)
            fimage = cv2.GaussianBlur(image,(ksize, ksize), sigma).astype(np.float64)
            chargeAreas = fimage > fimage.mean()*chargeratio

            for edge in Graph.edges():
                p1, p2, = tuple(atoms[edge[0]]), tuple(atoms[edge[1]])
                cv2.line(lineImg, p1, p2, 1)

                if ~((lineImg & chargeAreas) == lineImg).all():
                    Graph.remove_edge(*edge)

                cv2.line(lineImg, p1,p2, 0)
            for n in [n for n in Graph.degree() if Graph.degree()[n] == 0]: Graph.remove_node(n)
            return np.array([atoms[n] for n in Graph.nodes()]), Graph

        def stageTwoPrune(atoms, Graph):
            def getAngle(v1, v2):
                return np.arctan2(np.linalg.norm(np.cross(v1, v2)), np.dot(v1, v2))

            def getInnerAngles(ps, G):
                """Returns a 2d array where the columns are [angle in rads, source edges, end edge 1, end edge 2]"""
                al = []
                for node in G.nodes():
                    neighbours = G.neighbors(node)
                    if len(neighbours) == 0 or len(neighbours)== 1: continue
                    for ind in range(len(neighbours)-1,0,-1):
                        index = ind - 1
                        l1 = ps[neighbours[ind]] - ps[node]
                        l2 = ps[neighbours[index]] - ps[node]
                        angle = getAngle(l1,l2)
                        al.append( [angle, node, neighbours[ind], neighbours[index]])
                return np.array(al)

            def removeAngles(angles):
                mean = angles[:,0].mean()
                std = angles[:,0].std()

                edgeset = set()
                for angle in angles:
                    if mean - 3*std < angle[0] < mean + 3*std: continue

                    edge = (int(angle[1]),int(angle[2]))
                    rdge = (int(angle[2]),int(angle[1]))
                    if edge not in edgeset and rdge not in edgeset:
                        edgeset.add(edge)
                    else:
                        if Graph.has_edge(*edge):
                            Graph.remove_edge(*edge)
                        else:
                            Graph.remove_edge(*rdge)

                    edge = (int(angle[1]),int(angle[3]))
                    rdge = (int(angle[3]),int(angle[1]))
                    if edge not in edgeset and rdge not in edgeset:
                        edgeset.add(edge)
                    else:
                        if Graph.has_edge(*edge):
                            Graph.remove_edge(*edge)
                        else:
                            Graph.remove_edge(*edge)
                return Graph

            return atoms, removeAngles(getInnerAngles(atoms, Graph))
        return stageTwoPrune(*stageOnePrune(points, getInitial(points)))

    def find_cycles(self, Image, Graph, points, reorder=True):
        #initialize vars
        ff = np.zeros(image.shape)
        img, G = Image.copy(), Graph.copy()
        node2point = {tuple(points[ind]):ind for ind in G.nodes()}

        def inNeighborhood(x, y, points, path):
            l = [(x, y), (x+1, y), (x+1, y+1),(x, y+1),(x-1, y+1),(x-1, y),(x-1, y-1),(x, y-1),(x+1, y-1)]
            for p in l:
                if p in points:
                    path.add(points[p])
                    return

        def reorderCycle(cycle):
            if len(cycle) is 0: return cycle
            remain = cycle[1:].tolist()
            path = [cycle[0]]
            i = 0
            while len(remain) is not 0:
                p = [val for val in remain if val in G.neighbors(path[-1])]
                if len(p) is 0: return []
                path.append(p[0])
                remain.remove(p[0])
            if path[0] not in G.neighbors(path[-1]): return []
            return path

        def floodfill(x, y):
            pfound = set()
            toFill = set()
            toFill.add((x,y))

            while len(toFill) is not 0:
                x, y = toFill.pop()
                if x < 0 or x >= ff.shape[0]: continue
                if y < 0 or y >= ff.shape[1]: continue
                if ff[x,y] == 1: continue
                ff[x,y] = 1
                inNeighborhood(y, x , node2point, pfound)
                toFill.add((x-1,y))
                toFill.add((x,y-1))
                toFill.add((x+1,y))
                toFill.add((x,y+1))
            return np.array(list(pfound))

        #draw lines on the image
        for edge in G.edges():
            se = [points[x] for x in edge]
            cv2.line(ff, tuple(se[0]), tuple(se[1]), 1, 1, 8)

        cycles = []
        while ff.any():
            i1,i2 = np.where(ff == 0)
            if len(i1) == 0 or len(i2) == 0: break
            cycle = floodfill(i1[0], i2[0])
            if reorder: cycle = reorderCycle(cycle)
            if len(cycle) != 0: cycles.append(cycle)
        self.cycles = np.array(cycles)
        return np.array(cycles)
