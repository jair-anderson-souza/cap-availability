FROM adoptopenjdk/openjdk14:x86_64-tumbleweed-jre-14.0.2_12

WORKDIR /app

COPY ./build/libs/cap-availability-1.0.0.jar app.jar

ENV JAVA_OPTS="-Xms1024m -Xmx1024m -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
