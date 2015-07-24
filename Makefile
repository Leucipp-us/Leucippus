CLASSPATH = ".:ij.jar"

all:
	javac -classpath $(CLASSPATH) Plugin_Frame.java

run: all
	imagej ~/git/thesis-notebooks/images/graphene1.png