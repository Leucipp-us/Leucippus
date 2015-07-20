CLASSPATH = ".:ij.jar"

.PHONY: run

all:
	javac -classpath $(CLASSPATH) Plugin_Frame.java

run:
	imagej ~/git/thesis-notebooks/images/au100.JPG