CLASSPATH = ".:ij.jar"

.PHONY: run

all:
	javac -classpath $(CLASSPATH) Plugin_Frame.java

run:
	java -cp ".:ij.jar" ij.ImageJ ~/git/thesisinvestigations/thesisInvestigations/graphene1.png