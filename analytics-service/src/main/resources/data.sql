
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

-- Isolated fixture dimension avoids overwriting metrics maintained by the Kafka consumer.
INSERT INTO analytics_events (id, event_id, event_type, source_service, aggregate_type,
 aggregate_id, schema_version, occurred_at, received_at, processing_status, payload)
SELECT md5('hms-dev-analytics-row-' || d.reservation_id)::uuid,
 md5('hms-dev-analytics-event-' || d.reservation_id)::uuid,
 'CONSULTATION_RESERVED', 'reception-service', 'RESERVATION', d.reservation_id::text, 1,
 d.visit_date + TIME '08:00', d.visit_date + TIME '08:00', 'PROCESSED',
 '{"developmentFixture":true}'
FROM dev_consultations d WHERE d.visit_date <= CURRENT_DATE
ON CONFLICT DO NOTHING;

INSERT INTO metric_aggregates (id, metric_name, bucket_type, bucket_start, bucket_end,
 dimension_key, dimension_value, event_count, numeric_total, version, created_at, updated_at)
SELECT md5('hms-dev-metric-' || day::date)::uuid, 'CONSULTATION_RESERVED', 'DAY',
 day::date::timestamp AT TIME ZONE 'UTC', (day::date + 1)::timestamp AT TIME ZONE 'UTC',
 'development_fixture', 'mock', 4, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM generate_series(CURRENT_DATE - 12, CURRENT_DATE, INTERVAL '1 day') day
ON CONFLICT DO NOTHING;

DROP VIEW dev_consultations;
RESET TIME ZONE;
