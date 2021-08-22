FROM adoptopenjdk/openjdk8-openj9:jre8u302-b08_openj9-0.27.0
RUN addgroup --system spring && adduser --system --group spring
USER spring:spring
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]