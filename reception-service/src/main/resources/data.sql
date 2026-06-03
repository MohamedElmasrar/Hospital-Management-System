-- Development patient record, independent of application accounts.
ALTER TABLE patients DROP COLUMN IF EXISTS user_id;

INSERT INTO patients
  (id, mrn, national_id, first_name, last_name, date_of_birth,
   gender, blood_group, phone, emergency_contact_phone, emergency_contact_name,
   allergies, chronic_conditions, is_active, date_created, date_modified)
VALUES
  ('20000000-0000-0000-0000-000000000001', 'MRN-2026-000001',
   'MA-BK123456', 'Nadia', 'Amrani',
   DATE '1992-08-16', 'FEMALE', 'A+', '+212 6 98 76 54 32', '+212 6 11 22 33 44',
   'Rachid Amrani', 'Penicillin', 'Mild asthma', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (mrn) DO UPDATE
SET national_id = EXCLUDED.national_id,
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

-- Additional development mock data. Existing fixture records are preserved.
-- PostgreSQL fixtures shared by reception, billing, and analytics.
-- IDs derive from the actual date and slot, so overlapping date windows stay consistent.
SET TIME ZONE 'Africa/Casablanca';
CREATE TEMP VIEW dev_consultations AS
SELECT
  day::date AS visit_date,
  slot,
  md5('hms-dev-reservation-' || day::date || '-' || slot)::uuid AS reservation_id,
  md5('hms-dev-invoice-' || day::date || '-' || slot)::uuid AS invoice_id,
  md5('hms-dev-order-' || day::date || '-' || slot)::uuid AS order_id,
  md5('hms-dev-patient-' || ((extract(doy FROM day)::int * 4 + slot) % 24 + 1))::uuid AS patient_id,
  CASE WHEN slot = 4 THEN '16000000-0000-0000-0000-000000000002'::uuid
       ELSE '16000000-0000-0000-0000-000000000001'::uuid END AS physician_id,
  md5('hms-dev-room-' || CASE WHEN slot = 4 THEN 2 ELSE 1 END)::uuid AS room_id,
  TIME '09:00' + (slot - 1) * INTERVAL '30 minutes' AS starts_at,
  TIME '09:30' + (slot - 1) * INTERVAL '30 minutes' AS ends_at,
  CASE WHEN slot = 1 THEN 'PENDING_PAYMENT'
       WHEN slot = 4 THEN CASE WHEN extract(isodow FROM day) = 1 THEN 'CANCELLED' ELSE 'PENDING_PAYMENT' END
       WHEN slot = 2 AND day::date = CURRENT_DATE THEN 'CHECKED_IN'
       WHEN slot = 3 AND day::date < CURRENT_DATE THEN 'COMPLETED'
       ELSE 'PAID' END AS status_code,
  slot IN (2, 3) AS is_paid
FROM generate_series(CURRENT_DATE - 12, CURRENT_DATE + 2, INTERVAL '1 day') day
CROSS JOIN generate_series(1, 4) slot;

-- Clinical records only: these patients have no application accounts.
INSERT INTO patients (id, mrn, national_id, first_name, last_name, date_of_birth,
 gender, blood_group, phone, is_active, date_created, date_modified)
SELECT md5('hms-dev-patient-' || i)::uuid, 'DEV-MRN-' || lpad(i::text, 4, '0'),
 'DEV-ID-' || lpad(i::text, 4, '0'),
 (ARRAY['Amal','Youssef','Hana','Mehdi','Lina','Anas'])[1 + (i - 1) % 6],
 'Demo ' || lpad(i::text, 2, '0'), DATE '1960-01-01' + i * 600,
 CASE WHEN i % 2 = 0 THEN 'MALE' ELSE 'FEMALE' END,
 (ARRAY['A+','O+','B+','AB+'])[1 + (i - 1) % 4],
 '+212600900' || lpad(i::text, 3, '0'), TRUE,
 CURRENT_TIMESTAMP - i * INTERVAL '1 day', CURRENT_TIMESTAMP
FROM generate_series(1, 24) i
ON CONFLICT DO NOTHING;

INSERT INTO reservation_categories (id, code, name, requires_physician, requires_prescription, is_active, date_created)
VALUES
 (md5('hms-dev-consultation-category')::uuid, 'CONSULTATION', 'Physician Consultation', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
 (md5('hms-dev-imaging-category')::uuid, 'DIAGNOSTIC_IMAGING', 'Diagnostic & Medical Imaging', FALSE, TRUE, TRUE, CURRENT_TIMESTAMP),
 (md5('hms-dev-laboratory-category')::uuid, 'LABORATORY', 'Laboratory Analysis', FALSE, TRUE, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO reservation_statuses (id, code, name, allows_queueing, is_terminal, is_active)
SELECT md5('hms-dev-status-' || code)::uuid, code, label, queueing, terminal, TRUE
FROM (VALUES
 ('PENDING_PAYMENT', 'Pending payment', FALSE, FALSE),
 ('PAID', 'Paid', TRUE, FALSE),
 ('CHECKED_IN', 'Checked in', TRUE, FALSE),
 ('CALLED', 'Called to room', FALSE, FALSE),
 ('IN_SERVICE', 'In examination', FALSE, FALSE),
 ('NO_SHOW', 'No show', FALSE, TRUE),
 ('COMPLETED', 'Completed', FALSE, TRUE),
 ('CANCELLED', 'Cancelled', FALSE, TRUE)
) s(code, label, queueing, terminal)
ON CONFLICT DO NOTHING;

INSERT INTO examination_rooms (id, room_code, name, department_id, is_active, date_created)
SELECT md5('hms-dev-room-' || n)::uuid, 'DEV-CARD-' || n, 'Development consultation room ' || n,
 '11000000-0000-0000-0000-000000000001', TRUE, CURRENT_TIMESTAMP
FROM generate_series(1, 2) n
ON CONFLICT DO NOTHING;

INSERT INTO service_schedules (id, department_id, practitioner_id, room_id, day_of_week,
 start_time, end_time, slot_duration_minutes, is_active)
SELECT md5('hms-dev-schedule-' || physician || '-' || weekday)::uuid,
 '11000000-0000-0000-0000-000000000001',
 ('16000000-0000-0000-0000-' || lpad(physician::text, 12, '0'))::uuid,
 md5('hms-dev-room-' || physician)::uuid, weekday, TIME '08:00', TIME '17:00', 30, TRUE
FROM generate_series(1, 2) physician
CROSS JOIN unnest(ARRAY['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY']) weekday
ON CONFLICT DO NOTHING;

INSERT INTO reservations (id, reservation_number, patient_id, category_id, department_id,
 practitioner_id, room_id, reservation_date, start_time, end_time, status_id, is_paid,
 billing_invoice_id, reason_for_visit, notes, receptionist_staff_id, created_at, updated_at)
SELECT d.reservation_id, 'DEV-CONS-' || to_char(d.visit_date, 'YYYYMMDD') || '-' || d.slot,
 d.patient_id, c.id, '11000000-0000-0000-0000-000000000001', d.physician_id, d.room_id,
 d.visit_date, d.starts_at, d.ends_at, s.id, d.is_paid,
 CASE WHEN d.is_paid THEN d.invoice_id END, 'Development: routine consultation',
 'Synthetic development fixture', '00000000-0000-0000-0000-000000000005',
 d.visit_date - INTERVAL '1 day', CURRENT_TIMESTAMP
FROM dev_consultations d
JOIN reservation_categories c ON c.code = 'CONSULTATION'
JOIN reservation_statuses s ON s.code = d.status_code
ON CONFLICT DO NOTHING;

INSERT INTO queue_priority_levels (id, code, name, weight, is_active)
VALUES
 (md5('hms-dev-normal-priority')::uuid, 'NORMAL', 'Standard queue', 10, TRUE),
 (md5('hms-dev-priority')::uuid, 'PRIORITY', 'Priority queue', 30, TRUE),
 (md5('hms-dev-urgent-priority')::uuid, 'URGENT', 'Urgent clinical need', 60, TRUE),
 (md5('hms-dev-emergency-priority')::uuid, 'EMERGENCY', 'Immediate emergency', 100, TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO daily_queues (id, department_id, room_id, queue_date, last_issued_sequence, is_open)
VALUES (md5('hms-dev-queue-' || CURRENT_DATE)::uuid,
 '11000000-0000-0000-0000-000000000001', md5('hms-dev-room-1')::uuid, CURRENT_DATE, 0, TRUE)
ON CONFLICT DO NOTHING;

WITH inserted AS (
 INSERT INTO queue_tickets (id, queue_id, reservation_id, ticket_code, sequence_number, priority_id, status, issued_at)
 SELECT md5('hms-dev-ticket-' || d.reservation_id)::uuid, q.id, d.reservation_id,
 'CONS-' || lpad((q.last_issued_sequence + 1)::text, 3, '0'), q.last_issued_sequence + 1,
 p.id, 'WAITING', CURRENT_TIMESTAMP
 FROM dev_consultations d
 JOIN daily_queues q ON q.room_id = d.room_id AND q.queue_date = d.visit_date
 JOIN reservations r ON r.id = d.reservation_id
 JOIN reservation_statuses s ON s.id = r.status_id AND s.code = 'CHECKED_IN'
 CROSS JOIN queue_priority_levels p
 WHERE d.visit_date = CURRENT_DATE AND d.slot = 2 AND p.code = 'NORMAL'
 ON CONFLICT DO NOTHING
 RETURNING queue_id, sequence_number
)
UPDATE daily_queues q SET last_issued_sequence = greatest(q.last_issued_sequence, i.sequence_number)
FROM inserted i WHERE q.id = i.queue_id;

-- Leave beds available to try the admission form without forcing a patient admission.
INSERT INTO beds (id, bed_code, ward, occupied)
SELECT md5('hms-dev-bed-' || n)::uuid, 'DEV-BED-' || n, 'Development ward', FALSE
FROM generate_series(1, 6) n
ON CONFLICT DO NOTHING;

WITH inserted AS (
 INSERT INTO admissions (id, patient_id, bed_id, reason, admitted_at)
 SELECT md5('hms-dev-admission-' || n)::uuid, md5('hms-dev-patient-' || n)::uuid,
 b.id, 'Development: observation', CURRENT_TIMESTAMP - n * INTERVAL '1 day'
 FROM generate_series(1, 2) n
 JOIN beds b ON b.id = md5('hms-dev-bed-' || n)::uuid AND NOT b.occupied
 ON CONFLICT DO NOTHING
 RETURNING patient_id, bed_id
)
UPDATE beds b SET occupied = TRUE, patient_id = i.patient_id
FROM inserted i WHERE b.id = i.bed_id;

DROP VIEW dev_consultations;
RESET TIME ZONE;
