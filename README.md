# Language
- [English](#english)
- [Tiếng Việt](#tiếng-việt)

---
## Tiếng Việt
# Niên luận ngành Mạng máy tính và Truyền thông dữ liệu - Hệ thống tìm kiếm sách từ các Open Resource

## Giới thiệu
- Đây là hệ thống giúp người dùng có thể tìm kiếm sách từ các Open Resource trên Internet.
- Hệ thống hiện tại tích hợp 4 nguồn tài nguyên mở
    + Open Library
    + VOER
    + Standard EBooks
    + Project Gutenberg

## Tính năng chính
- Người dùng nhập keyword vào ô input và Enter để tiến hành tìm kiếm.
- Hiển thị kết quả tìm kiếm (sách tiếng Việt + sách tiếng Anh) tại giao diện người dùng.
- Sẽ có 2 chế độ lọc để hiển thị kết quả tìm kiếm:
    + Cơ bản: Hiển thị tất cả các hệ thống kết quả tìm kiếm được.
    + Nâng cao: Hiển thị kết quả có tiêu đề trùng khớp hoàn toàn với keyword.

## Cài đặt

### Yêu cầu
- Docker Desktop
- Trình duyệt web (Google Chrome, Edge, Firefox)

### Hướng dẫn cài đặt
1. Cài đặt Docker Desktop tại đường dẫn sau: https://docs.docker.com/desktop/setup/install/windows-install/

2. Sau khi cài đặt thành công Docker Deskop thì tiến hành đăng nhập tài khoản Docker.

3. Tạo thư mục có tên CTUBookSearch (tên có thể thay đổi tùy theo ý người cài đặt).

4. Tạo file docker-compose.yml bên trong thư mục vừa tạo, có nội dung như sau:

```yaml
    version: '3.8'

    services:

    app:
        image: vinee04/booksearch-1.0.0:latest
        container_name: ctu-booksearch
        ports:
        - "8080:8080"
        depends_on:
        - rabbitmq
        environment:
        SPRING_RABBITMQ_HOST: rabbitmq
        SPRING_RABBITMQ_PORT: 5672

    rabbitmq:
        image: rabbitmq:3-management
        container_name: rabbitmq
        ports:
        - "5672:5672"
        - "15672:15672"
```

5. Mở Terminal và di chuyển vào thư mục đã tạo.

6. Chạy lệnh sau để pull image từ Docker Hub về + tạo container + start container: 
    docker-compose up

## Cấu trúc thư mục
- `/config/`: các lớp cấu hình liên quan đến RabbitMQ và WebSocket 
- `/consumer/`: lớp Listener lấy kết quả tìm kiếm trong ResultQueue đẩy qua WebSocket để push qua cho Client
- `/controller/`: các lớp Controller để nhận request và trả response
- `/model/`: các lớp định nghĩa dữ liệu
- `/service/`: các lớp xử lý trong hệ thống (sửa lỗi chính tả, dịch keyword, tìm kiếm sách)
- `/static/`: các file CSS, JS, image sử dụng trong hệ thống
- `/templates/`: file HTML hiển thị giao diện người dùng

## Liên hệ tác giả
- Họ và tên: Lưu Trần Nhã Khuê
- MSSV: B2204942
- Email: luutrannhakhue1312@gmail.com
- Phone: 0918 757 627

## English
# Computer Networks and Data Communications Thesis Project - Book Search Systems from Open Resources

## Introduction
- This system allows users to search for books from Open Resources available on the Internet.
- Currently, the system integrates 4 Open Resources:
  + Open Library  
  + VOER  
  + Standard Ebooks  
  + Project Gutenberg  

## Main Features
- Users enter a keyword and press Enter to search.
- Display search results (Vietnamese + English books) on the user interface.
- Two filtering modes are available:
  - **Basic**: Show all search results.
  - **Advanced**: Show results with titles that exactly match the keyword.

## Installation

### Requirements
- Docker Desktop 
- Web browser (Chrome, Edge, Firefox)

### Setup Guide
1. Install Docker Desktop: https://docs.docker.com/desktop/setup/install/windows-install/

2. Log in to your Docker account after installation.

3. Create a folder (e.g., `CTUBookSearch`).

4. Create a file named `docker-compose.yml` inside that folder:

    ```yaml
    version: '3.8'

    services:

    app:
        image: vinee04/booksearch-1.0.0:latest
        container_name: ctu-booksearch
        ports:
        - "8080:8080"
        depends_on:
        - rabbitmq
        environment:
        SPRING_RABBITMQ_HOST: rabbitmq
        SPRING_RABBITMQ_PORT: 5672

    rabbitmq:
        image: rabbitmq:3-management
        container_name: rabbitmq
        ports:
        - "5672:5672"
        - "15672:15672"

5. Open Terminal and navigate to the folder.

6. Run the following command to pull images and start containers:
    docker-compose up

## Project Structure
- `/config/`: configuration classes for RabbitMQ and WebSocket
- `/consumer/`: Listener that receives results from ResultQueue and pushes them to clients via WebSocket
- `/controller/`: handle requests and responses
- `/model/`: data models
- `/service/`: business logic (spell correction, keyword translation, book search)
- `/static/`: CSS, JS, images
- `/templates/`: HTML UI templates

## Contact
- Fullname: Lưu Trần Nhã Khuê
- Student ID: B2204942
- Email: luutrannhakhue1312@gmail.com
- Phone: 0918 757 627