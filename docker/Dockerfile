FROM phusion/baseimage:18.04-1.0.0

RUN DEBIAN_FRONTEND=noninteractive apt update && apt upgrade -y
RUN DEBIAN_FRONTEND=noninteractive apt install -y -q curl
RUN DEBIAN_FRONTEND=noninteractive apt install -y -q ca-certificates-java openjdk-17-jdk

ENV TOMCAT_VERSION 9.0.54

RUN curl "http://archive.apache.org/dist/tomcat/tomcat-9/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz" -o /tmp/tomcat9.tar.gz

RUN tar -xzf /tmp/tomcat9.tar.gz -C /tmp
RUN rm -rf /tmp/apache-tomcat-$TOMCAT_VERSION/webapps/*
RUN mkdir -p /usr/share/tomcat9
RUN mv /tmp/apache-tomcat-$TOMCAT_VERSION/* /usr/share/tomcat9

## tomcat configuration
RUN mkdir /etc/service/tomcat
ADD tomcat9-run.sh /etc/service/tomcat/run
ADD ROOT.war /usr/share/tomcat9/webapps/ROOT.war
RUN chmod +x /etc/service/tomcat/run
ADD server.xml /usr/share/tomcat9/conf/server.xml
RUN chown root.root /usr/share/tomcat9/conf/server.xml

EXPOSE 8080
CMD ["/sbin/my_init"]
