import cv2
import os
import sys
import json
import numpy as np
from controller import Controller

class Communicator(object):
	""" Communicator class
	Designed to send and receive data from the Leucippus ImageJ plugin
	"""
	def __init__(self):
		self.con = Controller()
		self.exit = False
		return

	def start(self):
		""" Start function
			Reads lines sent from the ImageJ Plugin
		"""
		while(not self.exit):
			line = sys.stdin.readline()
			if len(line) != 0:
				message = json.loads(line)
				self.routeMessage(message)


	def routeMessage(self, message):
		""" This function converts the json message sent from the ImageJ Plugin
			and converts it to the proper type and calls the appropriate
			function. It then sends the results back to the ImageJ Plugin.
		"""
		if not 'type' in message:
			print >> sys.stderr, "Invalid message"
			return

		elif message['type'] == 'EXIT':
			self.exit = True
			print '{"type":"exit"}\n'
			sys.stdout.flush()

		elif message['type'] == 'INITIAL_POINTS':
			imagedata = np.array(message['image']['data'])
			image = imagedata.reshape((message['image']['height'],
								message['image']['width'])).astype(np.uint8)

			retset = self.con.getDetections(image,
											message['sigma'],
											message['blocksize'])
			print json.dumps(retset)
			sys.stdout.flush()

		elif message['type'] == 'CONSTRAIN_POINTS':
			imagedata = np.array(message['image']['data'])
			image = imagedata.reshape((message['image']['height'],
								message['image']['width'])).astype(np.uint8)

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
			retset = self.con.constrainDetections(image,
							np.array(message['pointset']),
							message['sigma'],
							message['blocksize'])
			print json.dumps(retset)
			sys.stdout.flush()
		else:
			print >> sys.stderr, message['type']
