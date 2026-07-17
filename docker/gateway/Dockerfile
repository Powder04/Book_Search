# ══════════════════════════════════════════════════════════════════
# Dockerfile cho book-search-gateway
#
# QUAN TRỌNG — build context PHẢI là thư mục GỐC của repo (nơi có
# pom.xml cha), KHÔNG phải docker/gateway/, vì đây là Maven multi-module
# project (book-search-common, book-search-gateway, book-search-worker
# đều khai báo trong <modules> của pom.xml gốc). Maven reactor cần thấy
# đủ pom.xml của cả 3 module thì mới build được, dù chỉ build gateway.
#
# Build từ thư mục gốc repo:
#   docker build -f docker/gateway/Dockerfile -t vinee04/book-search-gateway:latest .
# ══════════════════════════════════════════════════════════════════

# ---------- Stage 1: build bằng Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom.xml của cả 3 module trước để tận dụng Docker layer cache
# cho bước tải dependency (chỉ invalidate khi pom.xml đổi, không phải
# khi sửa code Java).
COPY pom.xml .
COPY book-search-common/pom.xml book-search-common/pom.xml
COPY book-search-gateway/pom.xml book-search-gateway/pom.xml
COPY book-search-worker/pom.xml book-search-worker/pom.xml

# -pl book-search-gateway -am : chỉ build gateway + các module nó
# phụ thuộc (common), không build worker.
RUN mvn -q -pl book-search-gateway -am dependency:go-offline || true

# Copy source code thật sự (common là dependency, gateway là module cần build)
COPY book-search-common/src book-search-common/src
COPY book-search-gateway/src book-search-gateway/src

RUN mvn -q -pl book-search-gateway -am clean package -DskipTests

# ---------- Stage 2: runtime nhẹ, chỉ có JRE ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Không chạy bằng root trong container
RUN useradd -r -u 1000 spring
USER spring

COPY --from=build /build/book-search-gateway/target/book-search-gateway-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
