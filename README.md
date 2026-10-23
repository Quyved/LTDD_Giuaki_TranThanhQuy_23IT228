# ECOMER - Ứng dụng quản lý sản phẩm

Ứng dụng quản lý sản phẩm viết bằng Kotlin và Jetpack Compose trên Android, sử dụng Firebase Cloud Firestore và Firebase Authentication.

## Tính năng chính

### Phân hệ Quản trị (Admin)
- Xem danh sách sản phẩm theo thời gian thực (Cloud Firestore).
- Thêm sản phẩm mới kèm hình ảnh (chọn từ thư viện ảnh thiết bị).
- Chỉnh sửa thông tin sản phẩm (Tên, Loại, Giá, Hình ảnh).
- Xóa sản phẩm với hộp thoại xác nhận.

### Phân hệ Người dùng (User)
- Đăng ký tài khoản mới và đăng nhập bằng Email / Mật khẩu.
- Xem danh mục sản phẩm (chế độ chỉ xem).
- Phân quyền người dùng dựa trên Cloud Firestore (`role: admin / user`).

## Cấu trúc dữ liệu Firestore
- Collection `Products`: `tenSanPham`, `loaiSanPham`, `gia`, `image`.
- Collection `Users`: `uid`, `email`, `role`.

## Cài đặt và chạy
1. Mở dự án trong Android Studio.
2. File cấu hình `google-services.json` đã có sẵn trong thư mục `app/`.
3. Chờ Gradle sync xong và bấm **Run** để khởi chạy ứng dụng.
