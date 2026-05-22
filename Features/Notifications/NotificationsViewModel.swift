import Foundation
import Combine
import SwiftUI

enum NotificationTab {
    case unread
    case all
}

struct AppNotificationItem: Identifiable, Hashable {
    let id: Int
    let referenceID: Int?
    let title: String
    let subtitle: String
    let timeAgo: String
    var isUnread: Bool
    let iconBackground: Color
    let iconEmoji: String
}

protocol NotificationsServiceProtocol {
    func fetchNotifications() async throws -> [AppNotificationItem]
    func markAllRead() async throws
    func markRead(id: Int) async throws
}

final class NotificationsMockService: NotificationsServiceProtocol {
    func fetchNotifications() async throws -> [AppNotificationItem] {
        try await Task.sleep(nanoseconds: 220_000_000)
        return [
            AppNotificationItem(
                id: 1,
                referenceID: 101,
                title: "Property approved",
                subtitle: "Your Skyline Residency listing has been approved.",
                timeAgo: "2026-05-22",
                isUnread: true,
                iconBackground: Color(hex: 0xDFF5E8),
                iconEmoji: "✅"
            ),
            AppNotificationItem(
                id: 2,
                referenceID: 205,
                title: "Visit scheduled",
                subtitle: "A new site visit was added for Palm Meadows.",
                timeAgo: "2026-05-21",
                isUnread: true,
                iconBackground: Color(hex: 0xFFEEEE),
                iconEmoji: "📅"
            ),
            AppNotificationItem(
                id: 3,
                referenceID: 333,
                title: "Deal update",
                subtitle: "Deal progressed to negotiation stage.",
                timeAgo: "2026-05-20",
                isUnread: false,
                iconBackground: Color(hex: 0xFFF3E0),
                iconEmoji: "🤝"
            )
        ]
    }

    func markAllRead() async throws {}

    func markRead(id: Int) async throws {
        _ = id
    }
}

@MainActor
final class NotificationsViewModel: ObservableObject {
    @Published var notifications: [AppNotificationItem] = []
    @Published var isLoading = false
    @Published var selectedTab: NotificationTab = .unread
    @Published var errorMessage: String?

    private let service: NotificationsServiceProtocol

    init(service: NotificationsServiceProtocol = NotificationsMockService()) {
        self.service = service
    }

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            notifications = try await service.fetchNotifications()
        } catch {
            errorMessage = "Unable to load notifications."
        }
        isLoading = false
    }

    var unreadCount: Int {
        notifications.count { $0.isUnread }
    }

    var displayedItems: [AppNotificationItem] {
        switch selectedTab {
        case .unread:
            return notifications.filter { $0.isUnread }
        case .all:
            return notifications
        }
    }

    var newItems: [AppNotificationItem] {
        displayedItems.filter(\.isUnread)
    }

    var earlierItems: [AppNotificationItem] {
        displayedItems.filter { !$0.isUnread }
    }

    func markAllRead() async {
        do {
            try await service.markAllRead()
            notifications = notifications.map { item in
                var updated = item
                updated.isUnread = false
                return updated
            }
        } catch {
            errorMessage = "Failed to mark all notifications as read."
        }
    }

    func markRead(_ item: AppNotificationItem) async {
        guard item.isUnread else { return }
        do {
            try await service.markRead(id: item.id)
            if let idx = notifications.firstIndex(where: { $0.id == item.id }) {
                notifications[idx].isUnread = false
            }
        } catch {
            errorMessage = "Failed to update this notification."
        }
    }
}
