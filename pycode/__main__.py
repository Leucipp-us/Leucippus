#This python files starts the python process that is used to do all
#computer vision computations
from communicator import Communicator

def main():
	comm = Communicator()
	comm.start()

if __name__ == '__main__':
	try:
		main()
	except (KeyboardInterrupt, SystemExit):
		exit(0)
