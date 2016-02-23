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
- jdk (javac)
- ij.jar (ImageJ jar file)

If you are using apt-get for your package manager you can install all of these
using the command:

`sudo apt-get install git make python python-numpy python-scipy python-opencv default-jdk`

If you are not using apt-get you can install the corresponding repositories
using your package manager. Note that ij.jar will be automatically downloaded
by the makefile when you build leucippus.

If you are using python3 as your version of choice then you may need to either
use a virtual environment or download python 2 from your package manager.

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
default ImageJ plugin location "~/.ImageJ/plugins". Note that if you do not set
the py2 variable correctly the plugin will not function properly.

Before attempting to use the plugin make sure you refresh your environment
variables using
`source ~/.bashrc`
or
`. ~/.bashrc`.

Now you're ready to start using the Leucippus plugin.
