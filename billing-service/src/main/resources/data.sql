-- Demo billing setup. Prices are sample amounts for development.
INSERT INTO cash_desks (id, desk_code, department_id, section, is_active, date_created, date_modified)
SELECT '30000000-0000-0000-0000-000000000001', 'DEMO-BILLING', '11000000-0000-0000-0000-000000000001', 'RECEPTION', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cash_desks WHERE desk_code = 'DEMO-BILLING');

INSERT INTO service_catalog (id, service_code, department_id, name, is_active, date_created, date_modified)
SELECT '31000000-0000-0000-0000-000000000001', 'DEMO-CONSULTATION', '11000000-0000-0000-0000-000000000001', 'Consultation (demo)', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM service_catalog WHERE service_code = 'DEMO-CONSULTATION');

INSERT INTO service_tariff_versions (id, catalog_item_id, unit_price, tax_rate, effective_from, date_created)
SELECT '32000000-0000-0000-0000-000000000001', id, 200.00, 0, '2020-01-01', CURRENT_TIMESTAMP
FROM service_catalog WHERE service_code = 'DEMO-CONSULTATION'
AND NOT EXISTS (SELECT 1 FROM service_tariff_versions WHERE catalog_item_id = service_catalog.id AND effective_to IS NULL);

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

INSERT INTO cash_desks (id, desk_code, department_id, section, is_active, date_created, date_modified)
VALUES (md5('hms-dev-cash-desk')::uuid, 'DEV-SEED-CASH', '11000000-0000-0000-0000-000000000001',
 'RECEPTION', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO service_catalog (id, service_code, department_id, name, is_active, date_created, date_modified)
VALUES (md5('hms-dev-catalog')::uuid, 'DEV-SEED-CONSULT', '11000000-0000-0000-0000-000000000001',
 'Development consultation', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO service_tariff_versions (id, catalog_item_id, unit_price, tax_rate, effective_from, date_created)
VALUES (md5('hms-dev-tariff')::uuid, md5('hms-dev-catalog')::uuid, 200.00, 0, DATE '2020-01-01', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Today's unpaid appointments have no invoice yet, so the Biller can try invoice creation.
INSERT INTO billable_orders (id, external_order_id, order_type, patient_id, practitioner_id,
 department_id, tariff_version_id, performed_at, date_created)
SELECT d.order_id, d.reservation_id, 'APPOINTMENT', d.patient_id, d.physician_id,
 '11000000-0000-0000-0000-000000000001', md5('hms-dev-tariff')::uuid,
 d.visit_date + d.starts_at, d.visit_date - INTERVAL '1 day'
FROM dev_consultations d
WHERE d.is_paid OR (d.slot = 1 AND d.visit_date < CURRENT_DATE - 2)
ON CONFLICT DO NOTHING;

INSERT INTO invoices (id, invoice_number, patient_id, cash_desk_id, issued_by_staff_id,
 status, issued_at, due_date, date_created, date_modified)
SELECT d.invoice_id, 'DEV-INV-' || to_char(d.visit_date, 'YYYYMMDD') || '-' || d.slot,
 d.patient_id, md5('hms-dev-cash-desk')::uuid, '00000000-0000-0000-0000-000000000007',
 CASE WHEN d.is_paid THEN 'PAID' ELSE 'ISSUED' END, d.visit_date + d.starts_at,
 d.visit_date + INTERVAL '7 days', d.visit_date + d.starts_at, CURRENT_TIMESTAMP
FROM dev_consultations d
JOIN billable_orders b ON b.id = d.order_id
ON CONFLICT DO NOTHING;

INSERT INTO invoice_line_items (id, invoice_id, billable_order_id, quantity, discount_rate, date_created)
SELECT md5('hms-dev-line-' || d.order_id)::uuid, d.invoice_id, d.order_id, 1, 0, d.visit_date + d.starts_at
FROM dev_consultations d
JOIN invoices i ON i.id = d.invoice_id
ON CONFLICT DO NOTHING;

INSERT INTO payments (id, invoice_id, payment_reference, cash_desk_id, cashier_id,
 amount, method, status, paid_at, date_created)
SELECT md5('hms-dev-payment-' || d.invoice_id)::uuid, d.invoice_id,
 'DEV-PAY-' || to_char(d.visit_date, 'YYYYMMDD') || '-' || d.slot,
 md5('hms-dev-cash-desk')::uuid, '00000000-0000-0000-0000-000000000007',
 200.00, CASE WHEN d.slot = 2 THEN 'CASH' ELSE 'CARD' END, 'COMPLETED',
 d.visit_date + d.starts_at, d.visit_date + d.starts_at
FROM dev_consultations d
JOIN invoices i ON i.id = d.invoice_id
WHERE d.is_paid
ON CONFLICT DO NOTHING;

INSERT INTO insurance_providers (id, name, code, contact_email, is_active, date_created, date_modified)
VALUES (md5('hms-dev-insurer')::uuid, 'Development Insurance', 'DEV-INSURER',
 'insurance@example.test', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO patient_insurance_policies (id, policy_number, insurance_provider_id, patient_id,
 coverage_rate, is_active, date_created, date_modified)
SELECT md5('hms-dev-policy-' || n)::uuid, 'DEV-POLICY-' || n, md5('hms-dev-insurer')::uuid,
 md5('hms-dev-patient-' || n)::uuid, 0.8000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM generate_series(1, 8) n
ON CONFLICT DO NOTHING;

-- One pending claim for a matching insured patient and unpaid invoice.
INSERT INTO insurance_claims (id, invoice_id, policy_id, claimed_amount, status,
 submitted_at, date_created, date_modified)
SELECT md5('hms-dev-insurance-claim')::uuid, i.id, p.id, 160.00, 'SUBMITTED',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM invoices i
JOIN patient_insurance_policies p ON p.patient_id = i.patient_id
WHERE i.invoice_number LIKE 'DEV-INV-%' AND i.status = 'ISSUED'
 AND p.id IN (SELECT md5('hms-dev-policy-' || n)::uuid FROM generate_series(1, 8) n)
ORDER BY i.issued_at, i.id
LIMIT 1
ON CONFLICT DO NOTHING;

DROP VIEW dev_consultations;
RESET TIME ZONE;
