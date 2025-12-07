-- =====================================================
-- TẠO TÀI KHOẢN ADMIN
-- =====================================================
-- Chạy file này trong PostgreSQL database để tạo admin user

-- Cách 1: Tạo admin user mới
-- Password: Admin@123 (đã hash bằng BCrypt)
INSERT INTO users (id, username, email, password, full_name, role, created_at, last_login)
VALUES 
(999, 'admin', 'admin@eduquiz.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Administrator', 'ADMIN', EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000)
ON CONFLICT (id) DO NOTHING;

-- Cách 2: Cập nhật user hiện có thành admin
-- Thay 'your_username' bằng username thực tế của bạn
-- UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';

-- =====================================================
-- VERIFY ADMIN USER
-- =====================================================
-- Kiểm tra xem admin đã được tạo chưa
SELECT id, username, email, full_name, role, created_at 
FROM users 
WHERE role = 'ADMIN';

-- =====================================================
-- THÔNG TIN ĐĂNG NHẬP
-- =====================================================
-- Username: admin
-- Password: Admin@123
-- Role: ADMIN
-- =====================================================
