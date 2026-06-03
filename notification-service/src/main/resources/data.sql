
-- Additional development mock data. Existing fixture records are preserved.
-- Read and unread examples for every seeded application account.
INSERT INTO notifications (id, recipient_id, source_service, title, message, created_at, read_at)
SELECT md5('hms-dev-notification-' || recipient || '-' || n)::uuid,
 ('00000000-0000-0000-0000-' || lpad(recipient::text, 12, '0'))::uuid,
 'development-seed',
 CASE n WHEN 1 THEN 'Development data is ready' ELSE 'Welcome to your demo account' END,
 CASE n WHEN 1 THEN 'Try your dashboard and the available work pages.'
        ELSE 'This is a synthetic notification for development testing.' END,
 CURRENT_TIMESTAMP - n * INTERVAL '1 hour',
 CASE WHEN n = 2 THEN CURRENT_TIMESTAMP - INTERVAL '30 minutes' END
FROM unnest(ARRAY[1,3,4,5,6,7,8]) recipient CROSS JOIN generate_series(1, 2) n
ON CONFLICT DO NOTHING;
