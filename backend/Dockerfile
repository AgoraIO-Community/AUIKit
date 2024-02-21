FROM --platform=linux/amd64 maven:3.9.2-eclipse-temurin-11 AS builder

WORKDIR /app

COPY mvn_settings.xml /root/.m2/settings.xml

COPY src ./src

COPY pom.xml ./

RUN mvn package -f pom.xml -Dmaven.test.skip=true -T 6


FROM --platform=linux/amd64 openjdk:11 AS Final

ARG VERSION
ARG BRANCH
ARG COMMIT_ID
ARG BUILD_DATE

ENV VERSION=${VERSION}
ENV BRANCH=${BRANCH}
ENV COMMIT_ID=${COMMIT_ID}
ENV BUILD_DATE=${BUILD_DATE}


WORKDIR /workspace

COPY --from=builder /app/target/*.jar /workspace/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
