# Leucippus

Leucippus is a Electron Microscopy analysis tool that is designed as a plugin
for the ImageJ tool stack that runs in the (Java Runtime Environment) JRE
and works in conjunction with python.

## Install
This installation guide assumes that you already have ImageJ installed and
working. It is important that you have run ImageJ at least once before trying
to install the plugin as the ImageJ plugin folder is not created until then.
If you do not then you can find ImageJ available on many package managers.

###Dependencies
To build and run Leucippus you need the following software:

- Git
- Make
- python 2.7
- numpy
- scipy
- python-opencv
- python-networkx
- jdk8 (javac 1.8)
- ij.jar (ImageJ jar file)

###Commands

If you are using apt for your package manager you can easily install all of these
using the command:

`sudo apt install git make python python-numpy python-scipy python-opencv python-networkx openjdk-8-jdk`

If you are not using apt or you are using a virtual environment you will need to
find the correct packages to build the code.

In order to build and install this repository you'll need to have run imageJ atleast once.
To build Leucippus, navigate to your repository directory and use `make`.
This will build and package the java classes and python code into a jar and zip file
respectively. Then just use `make install` in order to place the files within the imageJ plugin.
