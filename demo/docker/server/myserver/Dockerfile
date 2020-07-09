FROM java:8-jdk-alpine

COPY server.jar /usr/local/
# COPY mysql-connector-java-5.1.39-bin.jar /usr/lib/jvm/java-1.8-openjdk/jre/lib/ext/
CMD ["java", "-jar", "/usr/local/server.jar"]
