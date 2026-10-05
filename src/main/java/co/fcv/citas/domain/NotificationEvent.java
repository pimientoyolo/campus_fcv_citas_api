package co.fcv.citas.domain;

import java.time.Instant;

public record NotificationEvent(
        Long id,
        String eventType,
        Long aggregateId,
        String payload,
        String status,
        int retryCount,
        Instant createdAt,
        Instant processedAt
) {}
