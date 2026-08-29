FROM openjdk:15.0.2-jdk-slim

WORKDIR /app

COPY ./build/libs/cap-api-1.0.0.jar cap-api.jar

ENV JAVA_OPTS="-Xms1024m -Xmx1024m -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar cap-api.jar"]
