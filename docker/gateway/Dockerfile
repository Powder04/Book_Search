FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY book-search-core/pom.xml book-search-core/pom.xml
COPY book-search-sources/pom.xml book-search-sources/pom.xml
COPY book-search-gateway/pom.xml book-search-gateway/pom.xml
COPY book-search-worker/pom.xml book-search-worker/pom.xml

RUN mvn -q -pl book-search-gateway -am dependency:go-offline || true

# Gateway chi phu thuoc book-search-core (khong dung book-search-sources), nen
# thay doi nguon du lieu (book-search-sources / book-search-worker) khong lam
# invalidate cache tu day tro xuong -> khong rebuild/push lai anh gateway.
COPY book-search-core/src book-search-core/src
COPY book-search-gateway/src book-search-gateway/src

RUN mvn -q -pl book-search-gateway -am clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd -r -u 1000 spring
USER spring

COPY --from=build /build/book-search-gateway/target/book-search-gateway-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
