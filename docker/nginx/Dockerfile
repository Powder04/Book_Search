FROM nginx:1.27-alpine

ENV GATEWAY_HOST=book-search-gateway \
    GATEWAY_PORT=8080

COPY default.conf.template /etc/nginx/templates/default.conf.template

COPY templates/index.html /usr/share/nginx/html/index.html
COPY static/css         /usr/share/nginx/html/css
COPY static/images      /usr/share/nginx/html/images
COPY static/javascript  /usr/share/nginx/html/javascript

EXPOSE 80
