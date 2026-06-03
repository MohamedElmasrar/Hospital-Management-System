# Notifications

A small per-user inbox. Auth, HR, reception, and billing publish JSON events to
`hms.notifications.events` after committing their business transaction. Change
`NOTIFICATION_KAFKA_TOPIC` in every participating service to rename the topic.

The Kafka message key is the recipient's auth user UUID. Employee and physician
IDs must be resolved to their linked `userId`; patients never receive application
notifications. Each event contains:

```json
{
  "eventId": "62e4a5a3-cecc-43a8-81a6-a14aa10a1a73",
  "recipientId": "00000000-0000-0000-0000-000000000007",
  "sourceService": "billing-service",
  "title": "Payment recorded",
  "message": "Your payment has been recorded.",
  "occurredAt": "2026-11-03T12:00:00Z"
}
```

Reuse the same event ID when retrying. The consumer uses that ID as the primary
key and skips messages already stored. Avoid patient names, clinical information,
credentials, or payment details. Publishing failures are logged; this version has
no transactional outbox.

Authenticated gateway endpoints:

- `GET /api/notifications?page=0&size=10` — newest first, maximum 100 per page.
- `GET /api/notifications/unread-count` — current user's unread count.
- `PATCH /api/notifications/{id}/read` — mark an owned notification read.
- `PATCH /api/notifications/read-all` — mark the current user's notifications read.

The API always takes the recipient from the authenticated JWT. There is no public
endpoint for sending notifications or accessing someone else's inbox.

Docker Compose runs the service on internal port 8006 with PostgreSQL and Kafka.
The frontend uses Orval-generated hooks and polls every 30 seconds while active;
notification messages load when the menu is open.
