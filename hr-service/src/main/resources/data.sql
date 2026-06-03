-- Development HR profiles for the users seeded by auth-service/data.sql.
-- UUIDs are fixed so this script is repeatable and relationships remain stable.

INSERT INTO medical_organization
  (id, name, legal_name, identifier, admin_id, created_at, updated_at)
VALUES
  ('10000000-0000-0000-0000-000000000001', 'Al Amal University Hospital',
   'Al Amal University Hospital Centre', 'HOSP-CASA-001',
   '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (identifier) DO UPDATE
SET name = EXCLUDED.name,
    legal_name = EXCLUDED.legal_name,
    admin_id = EXCLUDED.admin_id,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO department
  (id, medical_organization_id, parent_department_id, name, description, date_created)
VALUES
  ('11000000-0000-0000-0000-000000000001',
   '10000000-0000-0000-0000-000000000001', NULL, 'Cardiology',
   'Cardiovascular consultation, diagnosis, and treatment.', CURRENT_TIMESTAMP),
  ('11000000-0000-0000-0000-000000000002',
   '10000000-0000-0000-0000-000000000001', NULL, 'Administration',
   'Hospital administration and support services.', CURRENT_TIMESTAMP),
  ('11000000-0000-0000-0000-000000000003',
   '10000000-0000-0000-0000-000000000001', NULL, 'Reception',
   'Patient registration, appointments, and front-desk services.', CURRENT_TIMESTAMP),
  ('11000000-0000-0000-0000-000000000004',
   '10000000-0000-0000-0000-000000000001', NULL, 'Human Resources',
   'Workforce operations and employee services.', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE
SET medical_organization_id = EXCLUDED.medical_organization_id,
    name = EXCLUDED.name,
    description = EXCLUDED.description;

-- gender: 0=Male, 1=Female; employment_status: 0=Active.
INSERT INTO employee
  (id, user_id, identifier, given_name, additional_name, family_name,
   honorific_prefix, birth_date, gender, employment_status, date_created, date_modified)
VALUES
  ('12000000-0000-0000-0000-000000000005',
   '00000000-0000-0000-0000-000000000001', 'EMP-CEO-001', 'Leila', NULL,
   'Chraibi', 'Ms.', DATE '1982-11-09', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('12000000-0000-0000-0000-000000000001',
   '00000000-0000-0000-0000-000000000003', 'EMP-DOC-001', 'Yasmine', NULL,
   'El Mansouri', 'Dr.', DATE '1985-04-12', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('12000000-0000-0000-0000-000000000002',
   '00000000-0000-0000-0000-000000000004', 'EMP-ADM-001', 'Omar', NULL,
   'Bennani', 'Mr.', DATE '1990-09-23', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('12000000-0000-0000-0000-000000000003',
   '00000000-0000-0000-0000-000000000005', 'EMP-REC-001', 'Salma', NULL,
   'Alaoui', 'Ms.', DATE '1994-01-17', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('12000000-0000-0000-0000-000000000004',
   '00000000-0000-0000-0000-000000000006', 'EMP-HR-001', 'Karim', NULL,
   'Tazi', 'Mr.', DATE '1988-07-05', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (identifier) DO UPDATE
SET user_id = EXCLUDED.user_id,
    given_name = EXCLUDED.given_name,
    family_name = EXCLUDED.family_name,
    honorific_prefix = EXCLUDED.honorific_prefix,
    birth_date = EXCLUDED.birth_date,
    gender = EXCLUDED.gender,
    employment_status = EXCLUDED.employment_status,
    date_modified = CURRENT_TIMESTAMP;

INSERT INTO employee_assignment
  (id, employee_id, department_id, job_title, start_date, end_date)
VALUES
  ('13000000-0000-0000-0000-000000000005', '12000000-0000-0000-0000-000000000005',
   '11000000-0000-0000-0000-000000000002', 'Hospital Administrator', DATE '2018-02-01', NULL),
  ('13000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001',
   '11000000-0000-0000-0000-000000000001', 'Consultant Cardiologist', DATE '2020-01-06', NULL),
  ('13000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000002',
   '11000000-0000-0000-0000-000000000002', 'Administrative Coordinator', DATE '2022-03-14', NULL),
  ('13000000-0000-0000-0000-000000000003', '12000000-0000-0000-0000-000000000003',
   '11000000-0000-0000-0000-000000000003', 'Senior Receptionist', DATE '2021-09-01', NULL),
  ('13000000-0000-0000-0000-000000000004', '12000000-0000-0000-0000-000000000004',
   '11000000-0000-0000-0000-000000000004', 'Head of Human Resources', DATE '2019-05-20', NULL)
ON CONFLICT (id) DO UPDATE
SET employee_id = EXCLUDED.employee_id,
    department_id = EXCLUDED.department_id,
    job_title = EXCLUDED.job_title,
    start_date = EXCLUDED.start_date,
    end_date = EXCLUDED.end_date;

-- contact_type: 0=Mobile; address_type: 0=Home.
INSERT INTO contact_point
  (id, employee_id, contact_type, telephone, email, is_preferred)
VALUES
  ('14000000-0000-0000-0000-000000000005', '12000000-0000-0000-0000-000000000005', 0, '+212 6 12 34 56 00', 'admin@email.com', TRUE),
  ('14000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001', 0, '+212 6 12 34 56 01', 'doctor@email.com', TRUE),
  ('14000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000002', 0, '+212 6 12 34 56 02', 'employee@email.com', TRUE),
  ('14000000-0000-0000-0000-000000000003', '12000000-0000-0000-0000-000000000003', 0, '+212 6 12 34 56 03', 'receptionist@email.com', TRUE),
  ('14000000-0000-0000-0000-000000000004', '12000000-0000-0000-0000-000000000004', 0, '+212 6 12 34 56 04', 'hr_head@email.com', TRUE)
ON CONFLICT (id) DO UPDATE
SET employee_id = EXCLUDED.employee_id,
    telephone = EXCLUDED.telephone,
    email = EXCLUDED.email,
    is_preferred = EXCLUDED.is_preferred;

INSERT INTO postal_address
  (id, employee_id, address_type, street_address, address_locality,
   address_region, postal_code, address_country)
VALUES
  ('15000000-0000-0000-0000-000000000005', '12000000-0000-0000-0000-000000000005', 0, '10 Avenue des FAR', 'Casablanca', 'Casablanca-Settat', '20000', 'MA'),
  ('15000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001', 0, '18 Rue Al Massira', 'Casablanca', 'Casablanca-Settat', '20250', 'MA'),
  ('15000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000002', 0, '42 Boulevard Zerktouni', 'Casablanca', 'Casablanca-Settat', '20060', 'MA'),
  ('15000000-0000-0000-0000-000000000003', '12000000-0000-0000-0000-000000000003', 0, '7 Rue Ibn Sina', 'Mohammedia', 'Casablanca-Settat', '28810', 'MA'),
  ('15000000-0000-0000-0000-000000000004', '12000000-0000-0000-0000-000000000004', 0, '25 Avenue Hassan II', 'Casablanca', 'Casablanca-Settat', '20000', 'MA')
ON CONFLICT (id) DO UPDATE
SET employee_id = EXCLUDED.employee_id,
    street_address = EXCLUDED.street_address,
    address_locality = EXCLUDED.address_locality,
    address_region = EXCLUDED.address_region,
    postal_code = EXCLUDED.postal_code,
    address_country = EXCLUDED.address_country;

INSERT INTO physician (id, employee_id, license_number)
VALUES ('16000000-0000-0000-0000-000000000001',
        '12000000-0000-0000-0000-000000000001', 'MED-MA-45821')
ON CONFLICT (license_number) DO UPDATE SET employee_id = EXCLUDED.employee_id;

INSERT INTO medical_specialty (id, name, description)
VALUES ('17000000-0000-0000-0000-000000000001', 'Cardiology',
        'Diagnosis and treatment of cardiovascular diseases.')
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name, description = EXCLUDED.description;

INSERT INTO physician_specialty (id, physician_id, medical_specialty_id, is_primary)
VALUES ('18000000-0000-0000-0000-000000000001',
        '16000000-0000-0000-0000-000000000001',
        '17000000-0000-0000-0000-000000000001', TRUE)
ON CONFLICT (id) DO UPDATE
SET physician_id = EXCLUDED.physician_id,
    medical_specialty_id = EXCLUDED.medical_specialty_id,
    is_primary = EXCLUDED.is_primary;

INSERT INTO employee_credentials
  (id, employee_id, credential_type, credential_number, expires_on)
VALUES
  ('19000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001', 'Medical license', 'MED-MA-45821', DATE '2028-12-31'),
  ('19000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000003', 'Patient privacy training', 'PPT-2026-0142', DATE '2027-06-30'),
  ('19000000-0000-0000-0000-000000000003', '12000000-0000-0000-0000-000000000004', 'HR professional certificate', 'HR-MA-9321', DATE '2029-03-31')
ON CONFLICT (id) DO UPDATE
SET employee_id = EXCLUDED.employee_id,
    credential_type = EXCLUDED.credential_type,
    credential_number = EXCLUDED.credential_number,
    expires_on = EXCLUDED.expires_on;

-- Additional development mock data. Existing fixture records are preserved.
INSERT INTO employee (id, user_id, identifier, given_name, family_name, birth_date,
 gender, employment_status, date_created, date_modified)
VALUES
 ('12000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000007',
  'DEV-EMP-BILLER', 'Adam', 'Demo', DATE '1991-03-21', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 ('12000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000008',
  'DEV-EMP-DOC2', 'Sara', 'Demo', DATE '1987-05-12', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO employee_assignment (id, employee_id, department_id, job_title, start_date)
VALUES
 (md5('hms-dev-biller-assignment')::uuid, '12000000-0000-0000-0000-000000000006',
  '11000000-0000-0000-0000-000000000002', 'Biller', CURRENT_DATE - 365),
 (md5('hms-dev-doctor2-assignment')::uuid, '12000000-0000-0000-0000-000000000007',
  '11000000-0000-0000-0000-000000000001', 'Cardiologist', CURRENT_DATE - 365)
ON CONFLICT DO NOTHING;

INSERT INTO physician (id, employee_id, license_number)
VALUES ('16000000-0000-0000-0000-000000000002',
 '12000000-0000-0000-0000-000000000007', 'DEV-MED-002')
ON CONFLICT DO NOTHING;

INSERT INTO physician_specialty (id, physician_id, medical_specialty_id, is_primary)
VALUES (md5('hms-dev-doctor2-specialty')::uuid, '16000000-0000-0000-0000-000000000002',
 '17000000-0000-0000-0000-000000000001', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO employee_shifts (id, employee_id, department_id, shift_date, starts_at, ends_at)
SELECT md5('hms-dev-shift-' || e.id || '-' || day::date)::uuid,
 e.id, a.department_id, day::date, TIME '08:00', TIME '16:00'
FROM employee e
JOIN employee_assignment a ON a.employee_id = e.id AND a.end_date IS NULL
CROSS JOIN generate_series(CURRENT_DATE - 7, CURRENT_DATE + 7, INTERVAL '1 day') day
WHERE e.identifier IN ('EMP-DOC-001', 'EMP-ADM-001', 'EMP-REC-001', 'EMP-HR-001', 'DEV-EMP-BILLER', 'DEV-EMP-DOC2')
 AND extract(isodow FROM day) <= 5
ON CONFLICT DO NOTHING;

INSERT INTO attendance_records (id, employee_id, attendance_date, checked_in_at, checked_out_at)
SELECT md5('hms-dev-attendance-' || employee_id || '-' || shift_date)::uuid,
 employee_id, shift_date, shift_date + TIME '08:00',
 CASE WHEN shift_date < CURRENT_DATE THEN shift_date + TIME '16:00' END
FROM employee_shifts
WHERE id = md5('hms-dev-shift-' || employee_id || '-' || shift_date)::uuid
 AND shift_date <= CURRENT_DATE
ON CONFLICT DO NOTHING;

INSERT INTO leave_requests (id, employee_id, starts_on, ends_on, reason, status, decision_reason)
SELECT md5('hms-dev-leave-' || e.id || '-' || n)::uuid, e.id,
 CURRENT_DATE + 7 * n, CURRENT_DATE + 7 * n + 1, 'Development: personal leave',
 CASE n WHEN 1 THEN 'PENDING' WHEN 2 THEN 'APPROVED' ELSE 'REJECTED' END,
 CASE n WHEN 2 THEN 'Development: cover arranged' WHEN 3 THEN 'Development: staffing required' END
FROM employee e CROSS JOIN generate_series(1, 3) n
WHERE e.identifier IN ('EMP-DOC-001', 'EMP-ADM-001', 'EMP-REC-001')
ON CONFLICT DO NOTHING;
