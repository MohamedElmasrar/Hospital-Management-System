-- Development seed users. The password for every account is: password
-- The value stored in password_hash is a BCrypt hash, never the plaintext password.
INSERT INTO users (id, email, password_hash, role, active)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE),
  ('00000000-0000-0000-0000-000000000002', 'patient@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PATIENT', TRUE),
  ('00000000-0000-0000-0000-000000000003', 'doctor@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', TRUE),
  ('00000000-0000-0000-0000-000000000004', 'employee@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', TRUE),
  ('00000000-0000-0000-0000-000000000005', 'receptionist@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'RECEPTIONIST', TRUE),
  ('00000000-0000-0000-0000-000000000006', 'hr_head@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'HR_HEAD', TRUE)
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    active = EXCLUDED.active;
