FROM librairy/tomcat:1.0
MAINTAINER Librairy <librairy.framework@gmail.com>

ENV LIBRAIRY_HOME /librairy

WORKDIR /usr/local/tomcat

ADD modeler-w2v.war /usr/local/tomcat/webapps/modeler-w2v.war

VOLUME $LIBRAIRY_HOME

EXPOSE 8080
CMD ["catalina.sh", "run"]