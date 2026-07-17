# ══════════════════════════════════════════════════════════════════
# Dockerfile cho book-search-frontend (nginx)
#
# Build context là chính thư mục docker/nginx/ (khác với gateway/worker
# ở trên vì nginx không cần build Maven, chỉ copy static + config):
#
#   cd docker/nginx
#   docker build -t vinee04/book-search-frontend:latest .
# ══════════════════════════════════════════════════════════════════
FROM nginx:1.27-alpine

# Địa chỉ Service của Gateway trong k8s (namespace book-search).
# Trùng với "book-search-gateway" Service định nghĩa trong
# book-search-full.yaml, port 8080 — có thể override bằng env khi
# chạy container (vd. docker-compose dùng tên service khác).
ENV GATEWAY_HOST=book-search-gateway \
    GATEWAY_PORT=8080

# nginx image chính thức tự envsubst mọi file .template trong
# /etc/nginx/templates/ ra /etc/nginx/conf.d/ khi container start.
COPY default.conf.template /etc/nginx/templates/default.conf.template

# index.html đã bỏ hết cú pháp Thymeleaf (th:href/th:src), dùng
# đường dẫn tĩnh thường vì nginx không xử lý được template engine.
COPY templates/index.html /usr/share/nginx/html/index.html
COPY static/css         /usr/share/nginx/html/css
COPY static/images      /usr/share/nginx/html/images
COPY static/javascript  /usr/share/nginx/html/javascript

EXPOSE 80
