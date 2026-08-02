FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY book-search-core/pom.xml book-search-core/pom.xml
COPY book-search-sources/pom.xml book-search-sources/pom.xml
COPY book-search-gateway/pom.xml book-search-gateway/pom.xml
COPY book-search-worker/pom.xml book-search-worker/pom.xml

RUN mvn -q -pl book-search-worker -am dependency:go-offline || true

COPY book-search-core/src book-search-core/src
COPY book-search-sources/src book-search-sources/src
COPY book-search-worker/src book-search-worker/src

RUN mvn -q -pl book-search-worker -am clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd -r -u 1000 spring
USER spring

COPY --from=build /build/book-search-worker/target/book-search-worker-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
