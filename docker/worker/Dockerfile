# ══════════════════════════════════════════════════════════════════
# Dockerfile cho book-search-worker
#
# Cùng lý do như gateway: build context PHẢI là thư mục GỐC repo.
#
# LƯU Ý: hiện tại book-search-worker chạy CẢ 4 @RabbitListener
# (VOERWorker, OpenLibraryWorker, ProjectGutenbergWorker,
# StandardEBooksWorker) trong CÙNG một jar/pod — không có
# @ConditionalOnProperty theo SOURCE_NAME. Nên image này chỉ cần build
# 1 lần, dùng chung cho toàn bộ Deployment "book-search-worker" trong
# k8s (không cần build/tag riêng theo từng nguồn).
#
# Build từ thư mục gốc repo:
#   docker build -f docker/worker/Dockerfile -t vinee04/book-search-worker:latest .
# ══════════════════════════════════════════════════════════════════

# ---------- Stage 1: build bằng Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY book-search-common/pom.xml book-search-common/pom.xml
COPY book-search-gateway/pom.xml book-search-gateway/pom.xml
COPY book-search-worker/pom.xml book-search-worker/pom.xml

# -pl book-search-worker -am : chỉ build worker + common (dependency),
# không đụng tới gateway.
RUN mvn -q -pl book-search-worker -am dependency:go-offline || true

COPY book-search-common/src book-search-common/src
COPY book-search-worker/src book-search-worker/src

RUN mvn -q -pl book-search-worker -am clean package -DskipTests

# ---------- Stage 2: runtime nhẹ, chỉ có JRE ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd -r -u 1000 spring
USER spring

COPY --from=build /build/book-search-worker/target/book-search-worker-1.0.0.jar app.jar

# Worker không mở port HTTP (chỉ có spring-boot-starter, không có
# starter-web) nên không cần EXPOSE.

ENTRYPOINT ["java", "-jar", "app.jar"]
