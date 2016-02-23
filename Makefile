CLASSPATH = ".:ij.jar"

.PHONY: run clean
.SILENT: run clean

all: leucippus Leuzippy.zip Leucippus_.jar

Leuzippy.zip:
	cd pycode; find . -name "*.py" -print | zip ../Leuzippy -@

Leucippus_.jar: leucippus
	find . -name '*.class' -print > classes.list; echo "plugins.config" >> classes.list
	jar cf Leucippus_.jar @classes.list

leucippus: ij.jar
	javac -classpath $(CLASSPATH) Leucippus_.java

ij.jar:
	wget imagej.nih.gov/ij/upgrade/ij.jar

install:
ifeq '$(py2)' ''
	@echo "Please set the py2 make variable using 'make install py2=/path/to/python2' to install Leucippus"
	@echo ""
else
	echo "export leupython=$(py2)" >> ~/.bashrc
	install Leucippus_.jar Leuzippy.zip ~/.imagej/plugins
endif

run:
	imagej ~/Documents/git/thesis-notebooks/images/graphene1.png

clean:
	find . -type f -name '*.class' -delete
	find . -type f -name '*.pyc' -delete
	rm classes.list
