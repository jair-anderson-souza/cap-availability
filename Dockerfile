FROM adoptopenjdk/openjdk15:ppc64le-ubuntu-jre15u-nightly
WORKDIR /app

COPY ./build/libs/cap-availability-1.0.0.jar app.jar

ENV JAVA_OPTS="-Xms1024m -Xmx1024m -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
