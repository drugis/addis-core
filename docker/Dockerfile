FROM phusion/baseimage

RUN DEBIAN_FRONTEND=noninteractive apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get upgrade -y
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y -q curl
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y -q openjdk-8-jdk


ENV TOMCAT_VERSION 8.5.34

RUN curl "http://archive.apache.org/dist/tomcat/tomcat-8/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz" -o /tmp/tomcat8.tar.gz

RUN tar -xzf /tmp/tomcat8.tar.gz -C /tmp
RUN rm -rf /tmp/apache-tomcat-$TOMCAT_VERSION/webapps/*
RUN mkdir -p /usr/share/tomcat8
RUN mv /tmp/apache-tomcat-$TOMCAT_VERSION/* /usr/share/tomcat8

## tomcat configuration
RUN mkdir /etc/service/tomcat
ADD tomcat8-run.sh /etc/service/tomcat/run
ADD ROOT.war /usr/share/tomcat8/webapps/ROOT.war
RUN chmod +x /etc/service/tomcat/run
ADD server.xml /usr/share/tomcat8/conf/server.xml
RUN chown root.root /usr/share/tomcat8/conf/server.xml

# add keystore and truststore
RUN mkdir /ssl
ADD ssl/keystore.jks /ssl/keystore.jks

EXPOSE 8080
CMD ["/sbin/my_init"]
