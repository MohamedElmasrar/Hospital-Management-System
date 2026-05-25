-- Development seed users. The password for every account is: password
-- The value stored in password_hash is a BCrypt hash, never the plaintext password.
CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique ON users (email);

INSERT INTO users (id, email, password_hash, role, active)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 0, TRUE),
  ('00000000-0000-0000-0000-000000000002', 'patient@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 1, TRUE),
  ('00000000-0000-0000-0000-000000000003', 'doctor@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 2, TRUE),
  ('00000000-0000-0000-0000-000000000004', 'employee@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 3, TRUE),
  ('00000000-0000-0000-0000-000000000005', 'receptionist@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 4, TRUE),
  ('00000000-0000-0000-0000-000000000006', 'hr_head@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 5, TRUE)
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    active = EXCLUDED.active;
