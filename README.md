# Leucippus

Leucippus is a Electron Microscopy analysis tool that is designed as a plugin
for the imagej tool stack that runs in the JRE and works in conjunction with python.

## Install
This installation guide assumes that you already have imagej installed and working.
If you do not then you can find imagej available on many package managers.

###Dependencies
To build and run Leucippus you need the following software:

-Git
-Make
-python
-numpy
-scipy
-python-opencv
-jdk (javac)

If you are using apt-get for your package manager you can install all of these
using the command:

`sudo apt-get install git make python python-numpy python-scipy python-opencv default-jdk`

If you are not using apt-get you can install the corresponding repositories
 using your package manager.

###Using package manager
If you are using a package manager for python, like conda or pip, please use it
to install numpy, scipy and opencv.

Using Anaconda the command would be:

`conda install numpy scipy opencv`

and using apt-get it would be:

`sudo apt-get install python-numpy python-scipy python-opencv`

Once this is done all you have to do is download the github repository and then
build and install it using:

```bash
make
make install py2=/path/to/python2.7
```

This will build the code and place the Leucippus jar and python code in the
default imagej plugin location "~/.imagej/plugins". Note that if you do not set
the py2 variable correctly the plugin will not function properly.
