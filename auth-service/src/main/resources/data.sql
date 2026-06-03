-- Development seed users. The password for every account is: password
-- The value stored in password_hash is a BCrypt hash, never the plaintext password.
CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique ON users (email);

-- Remove legacy patient logins while preserving the ordinals of staff roles.
DELETE FROM refresh_token WHERE user_id IN (SELECT id FROM users WHERE role = 1);
DELETE FROM users WHERE role = 1;

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role BETWEEN 0 AND 6);

INSERT INTO users (id, email, password_hash, role, active)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 0, TRUE),
  ('00000000-0000-0000-0000-000000000003', 'doctor@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 2, TRUE),
  ('00000000-0000-0000-0000-000000000004', 'employee@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 3, TRUE),
  ('00000000-0000-0000-0000-000000000005', 'receptionist@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 4, TRUE),
  ('00000000-0000-0000-0000-000000000006', 'hr_head@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 5, TRUE),
  ('00000000-0000-0000-0000-000000000007', 'biller@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 6, TRUE)
ON CONFLICT (email) DO UPDATE
SET id = EXCLUDED.id,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    active = EXCLUDED.active;

-- Additional development mock data. Existing fixture records are preserved.
-- A second physician lets you test doctor scoping and schedule filters.
INSERT INTO users (id, email, password_hash, role, active)
VALUES ('00000000-0000-0000-0000-000000000008', 'doctor2@email.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq', 2, TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO audit_logs (id, actor_user_id, action, target_user_id, created_at)
SELECT md5('hms-dev-audit-' || i)::uuid,
 '00000000-0000-0000-0000-000000000001', 'USER_CREATED',
 ('00000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
 CURRENT_TIMESTAMP - i * INTERVAL '1 day'
FROM generate_series(3, 8) i
ON CONFLICT DO NOTHING;
