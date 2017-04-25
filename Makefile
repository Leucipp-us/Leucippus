CLASSPATH = ".:ij.jar"

.PHONY: run clean
.SILENT: run clean

all: Leucippus_.class Leuzippy.zip Leucippus_.jar

Leuzippy.zip: $(shell find -name "*.py" -type f)
	cd pycode; find . -name "*.py" -print | zip ../Leuzippy -@

Leucippus_.jar: Leucippus_.class classes.list
	jar cf Leucippus_.jar @classes.list

classes.list: $(shell find -name "*.class" -type f)
	find . -name '*.class' -print > classes.list; echo "plugins.config" >> classes.list

Leucippus_.class: ij.jar $(shell find -name "*.java" -type f) layout/SpringUtilities.class
	javac -classpath $(CLASSPATH) Leucippus_.java

layout/SpringUtilities.class:
	javac layout/SpringUtilities.java

ij.jar:
	wget imagej.nih.gov/ij/upgrade/ij.jar

install: Leuzippy.zip Leucippus_.jar
	install Leucippus_.jar Leuzippy.zip ~/.imagej/plugins

clean:
	find . -type f -name '*.class' -delete
	find . -type f -name '*.pyc' -delete
	rm classes.list
