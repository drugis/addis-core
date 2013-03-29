TrialVerse
==========

Running
-------

	sudo apt-get install openjdk-7-jdk maven postgresql postgresql-client postgresql-contrib-9.1 libsaxonb-java
	sudo update-alternatives --config java # choose java 7

	sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'develop'"
	sudo -u postgres createuser -S -D -R trialverse
	sudo -u postgres psql -c "ALTER USER trialverse WITH PASSWORD 'develop'"

	cd dbms
	./rebuild.sh
	cd ..

	mvn jetty:run
