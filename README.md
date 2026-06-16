# StoryLoom Archive

StoryLoom Archive là một ứng dụng thư viện số dùng để lưu trữ, tra cứu, đọc và quản lý các tác phẩm văn học dân gian, thần thoại và các văn bản thuộc phạm vi public-domain. Ứng dụng được xây dựng theo mô hình Spring MVC server-rendered: backend xử lý request, truy vấn dữ liệu, sau đó render HTML bằng Mustache template.

## Mục tiêu project

Project mô phỏng một digital archive nhỏ với các chức năng cơ bản:

- Quản lý metadata sách.
- Lưu trữ file sách và ảnh bìa.
- Tìm kiếm, duyệt danh mục và xem chi tiết sách.
- Đọc hoặc tải sách.
- Cho phép người dùng đăng ký, đăng nhập, bookmark sách, tạo shelf và ghi chú cá nhân.
- Cho phép admin upload sách mới vào hệ thống.

## Tính năng chính

### Người dùng ẩn danh

- Xem trang chủ.
- Duyệt sách theo catalog, author, category.
- Tìm kiếm sách.
- Xem trang chi tiết sách.
- Xem các trang thông tin như About, FAQ, Terms, Privacy.

### Người dùng đã đăng nhập

- Bookmark sách.
- Xem danh sách bookmark cá nhân.
- Tạo shelf/collection.
- Thêm sách vào shelf.
- Ghi chú riêng cho từng sách.

### Admin

- Truy cập trang admin.
- Thêm metadata sách.
- Upload cover image.
- Upload text file.
- Upload EPUB file.

## Công nghệ sử dụng

- Java 21
- Spring Boot 4.0.5
- Spring MVC
- Spring Security
- Spring Data JPA
- Mustache
- PostgreSQL 15
- MinIO
- Bucket4j
- Maven Wrapper
- Docker Compose

## Kiến trúc tổng quan

```text
Browser
   |
   v
Spring MVC Controller
   |
   |-- Mustache Template -> HTML
   |
   |-- Spring Data JPA -> PostgreSQL
   |
   |-- MinIO Client -> MinIO Object Storage
   |
   |-- Spring Security -> Login, session, role, CSRF
```

Project dùng mô hình MVC truyền thống, không phải kiến trúc REST API + SPA. Các controller trả về tên view, ví dụ `return "read"` nghĩa là render file `read.mustache`.

## Cấu trúc thư mục

```text
StoryLoom-Archive/
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── uploads/
├── src/
│   ├── main/
│   │   ├── java/com/storyloom/archive/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── metadata.json
│   │       ├── static/
│   │       └── templates/
│   └── test/
└── README.md
```

Vai trò các thư mục chính:

- `config/`: cấu hình bảo mật, web resource, rate limit, seed/import dữ liệu.
- `controller/`: xử lý request và điều hướng view.
- `model/`: JPA entity.
- `repository/`: Spring Data JPA repository.
- `service/`: logic phụ trợ như lưu file, rate limit, load user cho Spring Security.
- `templates/`: giao diện Mustache.
- `static/`: CSS và JavaScript.
- `metadata.json`: dữ liệu sách mẫu để seed database.

## Yêu cầu môi trường

- JDK 21
- Docker Desktop hoặc Docker Engine
- Maven không bắt buộc cài riêng vì project có Maven Wrapper

Kiểm tra Java:

```bash
java -version
```

Project đang khai báo Java 21 trong `pom.xml`:

```xml
<java.version>21</java.version>
```

## Cấu hình mặc định

File cấu hình chính:

```text
src/main/resources/application.properties
```

Giá trị local mặc định:

| Thành phần | Giá trị |
|---|---|
| App URL | `https://localhost:8443` |
| PostgreSQL URL | `jdbc:postgresql://localhost:5432/storyloom` |
| PostgreSQL database | `storyloom` |
| PostgreSQL username | `storyloom_admin` |
| PostgreSQL password | `secretpassword` |
| MinIO API | `http://127.0.0.1:9000` |
| MinIO Console | `http://localhost:9001` |
| MinIO username | `storyloom_admin` |
| MinIO password | `secretpassword` |
| MinIO bucket | `storyloom-archive` |

Lưu ý: các thông tin trên đang phục vụ demo local. Khi public repo hoặc deploy, nên chuyển credential sang environment variables và không commit secret thật.

## Cài đặt và chạy local

### 1. Clone project

```bash
git clone <repository-url>
cd StoryLoom-Archive
```

### 2. Khởi động PostgreSQL và MinIO

```bash
docker compose up -d
```

Kiểm tra container:

```bash
docker ps
```

Docker Compose sẽ khởi động:

- PostgreSQL: `localhost:5432`
- MinIO API: `localhost:9000`
- MinIO Console: `localhost:9001`

Lưu ý cho macOS/Linux: `docker-compose.yml` hiện mount MinIO vào `C:/StoryLoom_Storage`. Nếu không chạy trên Windows, cần đổi volume path này sang đường dẫn phù hợp, ví dụ:

```yaml
volumes:
  - ./minio-data:/data
```

### 3. Tạo bucket MinIO

1. Mở MinIO Console:

```text
http://localhost:9001
```

2. Đăng nhập:

```text
Username: storyloom_admin
Password: secretpassword
```

3. Tạo bucket:

```text
storyloom-archive
```

4. Cấu hình quyền đọc object phù hợp cho demo local, vì template hiện truy cập file trực tiếp qua URL MinIO.

### 4. Chạy ứng dụng

Windows:

```bash
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Mở ứng dụng:

```text
https://localhost:8443
```

Ứng dụng dùng SSL self-signed certificate cho local, nên browser có thể hiển thị cảnh báo bảo mật. Khi demo local có thể chọn tiếp tục truy cập.

## Seed dữ liệu

Khi database chưa có sách, `GutenbergImporter` sẽ đọc:

```text
src/main/resources/metadata.json
```

và import metadata sách vào database.

Cơ chế hiện tại:

- Nếu bảng sách trống, importer sẽ chạy.
- Nếu database đã có sách, importer sẽ bỏ qua.
- Metadata seed tạo bản ghi sách; file cover/text/epub cần được upload qua admin hoặc có sẵn trong MinIO với đúng object path.

## Tạo tài khoản admin

Project hiện chưa seed sẵn tài khoản admin. Có thể tạo admin thủ công như sau:

1. Đăng ký tài khoản mới tại:

```text
https://localhost:8443/register
```

2. Mở PostgreSQL shell:

```bash
docker exec -it storyloom-db psql -U storyloom_admin -d storyloom
```

3. Cập nhật role cho tài khoản vừa đăng ký:

```sql
UPDATE users
SET role = 'ROLE_ADMIN'
WHERE email = 'admin@example.com';
```

4. Đăng xuất và đăng nhập lại.
5. Truy cập trang admin:

```text
https://localhost:8443/admin
```

## Các route quan trọng

| Route | Mô tả |
|---|---|
| `/` | Trang chủ |
| `/search` | Trang search |
| `/search/results` | Kết quả tìm kiếm |
| `/catalog/authors` | Duyệt theo tác giả |
| `/catalog/titles` | Duyệt theo tiêu đề |
| `/category?name=...` | Duyệt theo category |
| `/author?name=...` | Duyệt theo author |
| `/book/{id}` | Chi tiết sách |
| `/read/{id}` | Trang đọc sách |
| `/login` | Đăng nhập |
| `/register` | Đăng ký |
| `/bookmarks` | Bookmark cá nhân |
| `/shelves` | Shelf cá nhân |
| `/admin` | Admin dashboard |
| `/admin/add-book` | Upload sách |

## Chạy test

Windows:

```bash
.\mvnw.cmd test
```

macOS/Linux:

```bash
./mvnw test
```

Ghi chú:

- Test hiện tại còn tối giản.
- Một số test có thể phụ thuộc cấu hình PostgreSQL local nếu chưa tách test profile.
- Nên bổ sung test nghiệp vụ cho register/login, bookmark, annotation, upload và search.

## Ảnh chụp màn hình

Hiện repository chưa có thư mục ảnh chụp màn hình. Có thể bổ sung sau theo gợi ý:

```text
docs/screenshots/
├── home.png
├── book-detail.png
├── reader.png
├── bookmarks.png
└── admin-upload.png
```

Sau khi có ảnh, có thể chèn vào README:

```md
![Trang chủ](docs/screenshots/home.png)
```

## Giới hạn hiện tại

Project hiện phù hợp cho demo local và đồ án tốt nghiệp. Một số giới hạn cần lưu ý:

- Credential và keystore demo đang nằm trong cấu hình local.
- URL MinIO còn hard-code theo `127.0.0.1`.
- Upload file cần được validate kỹ hơn ở server-side.
- Category đang lưu dạng chuỗi, chưa tách bảng riêng.
- Pagination/search còn có chỗ load toàn bộ dữ liệu rồi phân trang trong memory.
- Test nghiệp vụ còn ít.
- Cần làm rõ chính sách public/private cho route đọc sách `/read/**`.
- Cần permit static resource như `/app.js` nếu muốn dùng JS trên trang public.

## Định hướng cải thiện

Các cải thiện nên ưu tiên:

1. Chuyển secret sang environment variables.
2. Thêm CSRF token cho form admin upload.
3. Validate upload file ở server-side.
4. Sửa luồng đọc sách TXT/EPUB cho nhất quán.
5. Tách logic nghiệp vụ từ controller xuống service.
6. Tách category thành entity riêng nếu cần hỗ trợ nhiều category/sách.
7. Dùng `Pageable` cho catalog/search.
8. Bổ sung test nghiệp vụ.
9. Tách profile `dev`, `test`, `prod`.
10. Bổ sung ảnh chụp màn hình và tài liệu demo.

## Ghi chú trình bày

Khi trình bày project, nên nhấn mạnh:

- Đây là ứng dụng MVC server-rendered.
- PostgreSQL lưu metadata, user, bookmark, shelf và annotation.
- MinIO lưu file sách và ảnh bìa.
- Spring Security xử lý login, session, role admin và CSRF.
- Project có thể mở rộng thêm service layer, test profile, migration và storage URL abstraction để sẵn sàng hơn cho production.
