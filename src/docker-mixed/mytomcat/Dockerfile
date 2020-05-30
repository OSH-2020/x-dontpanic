FROM tomcat:7
ENV WORK_PATH /usr/local/
COPY server.jar /usr/local/
COPY run.sh /usr/local/
RUN chmod a+x /usr/local/run.sh
CMD ["sh", "/usr/local/run.sh"]
