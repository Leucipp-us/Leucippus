CLASSPATH = ".:ij.jar"

.PHONY: run

all:
	javac -classpath $(CLASSPATH) Plugin_Frame.java

run:
	imagej ~/git/thesisinvestigations/thesisInvestigations/graphene1.png