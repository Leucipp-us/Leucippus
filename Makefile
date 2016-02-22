CLASSPATH = ".:ij.jar"

.PHONY: run clean
.SILENT: run clean

all: leucippus Leuzippy.zip Leucippus_.jar

Leuzippy.zip:
	cd pycode; find . -name "*.py" -print | zip ../Leuzippy -@

Leucippus_.jar: leucippus
	find . -name '*.class' -print > classes.list; echo "plugins.config" >> classes.list
	jar cf Leucippus_.jar @classes.list

leucippus:
	javac -classpath $(CLASSPATH) Leucippus_.java

run:
	imagej ~/Documents/git/thesis-notebooks/images/graphene1.png

clean:
	find . -type f -name '*.class' -delete
	find . -type f -name '*.pyc' -delete
	rm classes.list
