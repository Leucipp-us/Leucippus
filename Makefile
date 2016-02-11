CLASSPATH = ".:ij.jar"

.PHONY: run clean
.SILENT: run clean

all:
	javac -classpath $(CLASSPATH) Leucippus_.java

run:
	imagej ~/Documents/git/thesis-notebooks/images/graphene1.png

clean:
	rm -f *.class */*.class
