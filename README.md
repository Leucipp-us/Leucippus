# Leucippus

Leucippus is a Electron Microscopy analysis tool that is designed as a plugin
for the imagej tool stack that runs in the JRE and works in conjunction with python.

## Install
This installation guide assumes that you already have imagej installed and working.
If you do not then you can find imagej available on many package managers.

###Using package manager
If you are using a package manager for python, like conda or pip, please use it
to install numpy, scipy and opencv.

Using Anaconda the command would be:

`conda install numpy scipy opencv`

and using apt-get it would be:

`sudo apt-get install python-numpy python-scipy python-opencv`

Once this is done all you have to do is download the github repository and then
build and install it using:

`make
python2install=/path/to/python2 sh install.sh`

This will build the code and place the Leucippus jar and python code in the
default imagej plugin location "~/.imagej/plugins"

<!-- ###Using a local install
The idea of this guide is to use a compile and use a local install of python2.7
if you do not already have a version that you can use or do not want to use a
version that already exists on your PC.

Before continuing with the following steps make sure that you have the following applications and libraries installed.
They should be available from a package manager on any distribution.

GCC
gfortran
BLAS
LAPACK
cmake

####local python
wget https://www.python.org/ftp/python/2.7.11/Python-2.7.11.tgz
tar zxf Python-2.7.11.tgz; cd Python-2.7.11
./configure
make profile-opt
mkdir ../pyinstall-dir; make install prefix=~/leucippus-install-dir/pyinstall-dir exec_prefix=~/leucippus-install-dir/pyinstall-dir
cd ..

####numpy
wget http://downloads.sourceforge.net/project/numpy/NumPy/1.10.4/numpy-1.10.4.tar.gz
tar zxf numpy-1.10.4.tar.gz
mkdir numpyinstall-dir; cd numpyinstall-dir
~/leucippus-install-dir/pyinstall-dir/bin/python setup.py build_ext -j 4 install --prefix ~/leucippus-install-dir/numpyinstall-dir
cp -r ~/leucippus-install-dir/numpyinstall-dir/lib/python2.7/site-packages/numpy ~/leucippus-install-dir/pyinstall-dir/lib/python2.7/site-packages/
cd ..

####scipy
wget https://github.com/scipy/scipy/releases/download/v0.17.0/scipy-0.17.0.tar.gz
tar zxf scipy-0.17.0.tar.gz
mkdir scipyinstall-dir; cd scipyinstall-dir
~/leucippus-install-dir/pyinstall-dir/bin/python setup.py build_ext -j 4 install --prefix ~/leucippus-install-dir/scipyinstall-dir
cp -r ~/leucippus-install-dir/scipyinstall-dir/lib/python2.7/site-packages/scipy ~/leucippus-install-dir/pyinstall-dir/lib/python2.7/site-packages/
cd ..

####opencv
wget http://downloads.sourceforge.net/project/opencvlibrary/opencv-unix/3.1.0/opencv-3.1.0.zip
unzip opencv-3.1.0 -->
