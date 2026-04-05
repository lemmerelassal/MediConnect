package com.pharma.service;

import com.pharma.entity.Notification;
import com.pharma.grpc.NotificationEvent;
import com.pharma.grpc.NotificationStreamRequest;
import com.pharma.grpc.NotificationStreamService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
@ApplicationScoped
public class NotificationStreamGrpcService implements NotificationStreamService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    // Store broadcast processors for each user
    private final Map<Long, BroadcastProcessor<NotificationEvent>> userStreams = new ConcurrentHashMap<>();

    @Override
    public Multi<NotificationEvent> streamNotifications(NotificationStreamRequest request) {
        Long userId = request.getUserId();
        
        // Create or get existing processor for this user
        BroadcastProcessor<NotificationEvent> processor = userStreams.computeIfAbsent(
            userId,
            id -> BroadcastProcessor.create()
        );

        // Send existing unread notifications first
        List<Notification> unreadNotifications = Notification.find(
            "user.id = ?1 and isRead = false order by createdAt desc",
            userId
        ).list();

        Multi<NotificationEvent> existingNotifications = Multi.createFrom().items(
            unreadNotifications.stream()
                .map(this::toNotificationEvent)
        );

        // Combine existing notifications with real-time stream
        Multi<NotificationEvent> combinedStream = Multi.createBy().concatenating()
            .streams(existingNotifications, processor);

        // Add heartbeat to keep connection alive
        Multi<NotificationEvent> heartbeat = Multi.createFrom().ticks()
            .every(Duration.ofSeconds(30))
            .map(tick -> NotificationEvent.newBuilder()
                .setType("HEARTBEAT")
                .setTitle("Connection alive")
                .setMessage("Heartbeat")
                .build());

        return Multi.createBy().merging().streams(combinedStream, heartbeat);
    }

    // Method to push new notifications to users
    public void pushNotification(Long userId, Notification notification) {
        BroadcastProcessor<NotificationEvent> processor = userStreams.get(userId);
        if (processor != null) {
            NotificationEvent event = toNotificationEvent(notification);
            processor.onNext(event);
        }
    }

    // Method to push notification to all connected users
    public void broadcastNotification(Notification notification) {
        Long userId = notification.user.id;
        pushNotification(userId, notification);
    }

    private NotificationEvent toNotificationEvent(Notification notification) {
        return NotificationEvent.newBuilder()
            .setId(notification.id)
            .setType(notification.type)
            .setTitle(notification.title)
            .setMessage(notification.message)
            .setRelatedEntityType(notification.relatedEntityType != null ? notification.relatedEntityType : "")
            .setRelatedEntityId(notification.relatedEntityId != null ? notification.relatedEntityId : 0)
            .setCreatedAt(notification.createdAt.format(FORMATTER))
            .build();
    }

    // Clean up streams for disconnected users
    public void removeUserStream(Long userId) {
        BroadcastProcessor<NotificationEvent> processor = userStreams.remove(userId);
        if (processor != null) {
            processor.onComplete();
        }
    }
}
