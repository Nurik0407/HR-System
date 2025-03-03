FROM openjdk:20-jdk-slim

WORKDIR /border

COPY build/libs/border-0.0.1-SNAPSHOT.jar ./build/libs/

CMD ["java","-jar","build/libs/border-0.0.1-SNAPSHOT.jar"]