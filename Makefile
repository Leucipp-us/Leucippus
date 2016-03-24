CLASSPATH = ".:ij.jar"

.PHONY: run clean
.SILENT: run clean

all: Leucippus_.class Leuzippy.zip Leucippus_.jar install

Leuzippy.zip: $(shell find -name "*.py" -type f)
	cd pycode; find . -name "*.py" -print | zip ../Leuzippy -@

Leucippus_.jar: Leucippus_.class
	find . -name '*.class' -print > classes.list; echo "plugins.config" >> classes.list
	jar cf Leucippus_.jar @classes.list

Leucippus_.class: ij.jar $(shell find -name "*.java" -type f) layout/SpringUtilities.class
	javac -classpath $(CLASSPATH) Leucippus_.java

layout/SpringUtilities.class:
	javac layout/SpringUtilities.java

ij.jar:
	wget imagej.nih.gov/ij/upgrade/ij.jar

install:
	install Leucippus_.jar Leuzippy.zip ~/.imagej/plugins
ifeq '$(py2)' ''
	@echo "Please set the py2 make variable using 'make install py2=/path/to/python2' to install Leucippus"
	@echo ""
else
	echo "export leupython=$(py2)" >> ~/.bashrc
endif

run:
	imagej ~/Documents/git/thesis-notebooks/images/graphene1.png

clean:
	find . -type f -name '*.class' -delete
	find . -type f -name '*.pyc' -delete
	rm classes.list
