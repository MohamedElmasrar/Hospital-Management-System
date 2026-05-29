-- Development patient profile for patient@email.com from auth-service/data.sql.
INSERT INTO patients
  (id, mrn, user_id, national_id, first_name, last_name, date_of_birth,
   gender, blood_group, phone, emergency_contact_phone, emergency_contact_name,
   allergies, chronic_conditions, is_active, date_created, date_modified)
VALUES
  ('20000000-0000-0000-0000-000000000001', 'MRN-2026-000001',
   '00000000-0000-0000-0000-000000000002', 'MA-BK123456', 'Nadia', 'Amrani',
   DATE '1992-08-16', 'FEMALE', 'A+', '+212 6 98 76 54 32', '+212 6 11 22 33 44',
   'Rachid Amrani', 'Penicillin', 'Mild asthma', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (mrn) DO UPDATE
SET user_id = EXCLUDED.user_id,
    national_id = EXCLUDED.national_id,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    date_of_birth = EXCLUDED.date_of_birth,
    gender = EXCLUDED.gender,
    blood_group = EXCLUDED.blood_group,
    phone = EXCLUDED.phone,
    emergency_contact_phone = EXCLUDED.emergency_contact_phone,
    emergency_contact_name = EXCLUDED.emergency_contact_name,
    allergies = EXCLUDED.allergies,
    chronic_conditions = EXCLUDED.chronic_conditions,
    is_active = EXCLUDED.is_active,
    date_modified = CURRENT_TIMESTAMP;
