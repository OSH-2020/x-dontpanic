FROM tomcat:7
ENV WORK_PATH /usr/local/

COPY mysql-connector-java-5.1.39-bin.jar /usr/local/openjdk-8/jre/lib/ext
COPY server.jar /usr/local/
COPY run.sh /usr/local/
RUN chmod a+x /usr/local/run.sh
CMD ["sh", "/usr/local/run.sh"]
