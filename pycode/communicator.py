import cv2
import sys
import json
import numpy as np
from controller import Controller

class Communicator(object):
	def __init__(self):
		self.con = Controller()
		self.exit = False
		return

	def start(self):
		while(not self.exit):
			line = sys.stdin.readline()
			message = json.loads(line)
			self.routeMessage(message)


	def routeMessage(self, message):
		if not 'type' in message:
			print >> sys.stderr, "Invalid message"
			return
		elif message['type'] == 'EXIT':
			self.exit = True
			print '{"type":"exit"}\n'
			sys.stdout.flush()

		elif message['type'] == 'GET_DETECTIONS':
			if 'lines' in message and message['lines']:
				for line in message['lines']:
					if line['type'] == 'BONDLENGTH':
						t = line['data']
						p1 = np.array((t[0],t[1]))
						p2 = np.array((t[2],t[3]))
						line = p2 - p1
						bl = np.sqrt(line.dot(line))
						self.con.setBondlength(bl)
						break

			imagedata = np.array(message['image']['data'])
			image = imagedata.reshape((message['image']['height'],
								message['image']['width'])).astype(np.uint8)

			retset = self.con.getDetections(image)
			print json.dumps(retset)
			sys.stdout.flush()
			
