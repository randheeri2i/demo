import Foundation
import Combine

enum ActivityState: String {
    case completed
    case pending
}

struct ActivityFeedItem: Identifiable, Hashable {
    let id: String
    let type: String
    let title: String
    let dateTime: String
    var status: ActivityState
}

struct ActivityDraft {
    var type: String = "call"
    var description: String = ""
    var scheduledTime: Date = Date()
}

protocol ActivitiesServiceProtocol {
    func fetchActivities() async throws -> [ActivityFeedItem]
    func markComplete(id: String) async throws
    func createActivity(_ draft: ActivityDraft) async throws -> ActivityFeedItem
}

final class ActivitiesMockService: ActivitiesServiceProtocol {
    private var items: [ActivityFeedItem] = [
        ActivityFeedItem(
            id: "501",
            type: "meeting",
            title: "Meet buyer for site shortlisting",
            dateTime: "May 22, 2026 at 10:00 AM",
            status: .pending
        ),
        ActivityFeedItem(
            id: "502",
            type: "site_visit",
            title: "Visit Palm Meadows with client",
            dateTime: "May 21, 2026 at 04:30 PM",
            status: .completed
        )
    ]

    func fetchActivities() async throws -> [ActivityFeedItem] {
        try await Task.sleep(nanoseconds: 220_000_000)
        return items
    }

    func markComplete(id: String) async throws {
        try await Task.sleep(nanoseconds: 120_000_000)
        if let idx = items.firstIndex(where: { $0.id == id }) {
            items[idx].status = .completed
        }
    }

    func createActivity(_ draft: ActivityDraft) async throws -> ActivityFeedItem {
        try await Task.sleep(nanoseconds: 180_000_000)

        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, yyyy 'at' h:mm a"
        let item = ActivityFeedItem(
            id: UUID().uuidString,
            type: draft.type,
            title: draft.description,
            dateTime: formatter.string(from: draft.scheduledTime),
            status: .pending
        )
        items.insert(item, at: 0)
        return item
    }
}

@MainActor
final class ActivitiesViewModel: ObservableObject {
    @Published var activities: [ActivityFeedItem] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    @Published var toastMessage: String?
    @Published var showToast = false

    let activityTypes: [(key: String, label: String)] = [
        ("call", "Call"),
        ("email", "Email"),
        ("meeting", "Meeting"),
        ("site_visit", "Site Visit"),
        ("property_inspection", "Property Inspection")
    ]

    private let service: ActivitiesServiceProtocol

    init(service: ActivitiesServiceProtocol = ActivitiesMockService()) {
        self.service = service
    }

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            activities = try await service.fetchActivities()
        } catch {
            errorMessage = "Unable to load activities."
        }
        isLoading = false
    }

    func markComplete(_ item: ActivityFeedItem) async {
        do {
            try await service.markComplete(id: item.id)
            activities = activities.map { activity in
                activity.id == item.id ? ActivityFeedItem(id: activity.id, type: activity.type, title: activity.title, dateTime: activity.dateTime, status: .completed) : activity
            }
            showTemporaryToast("Activity marked as completed")
        } catch {
            showTemporaryToast("Failed to update activity")
        }
    }

    func schedule(_ draft: ActivityDraft) async {
        do {
            let created = try await service.createActivity(draft)
            activities.insert(created, at: 0)
            showTemporaryToast("Activity scheduled")
        } catch {
            showTemporaryToast("Failed to schedule activity")
        }
    }

    private func showTemporaryToast(_ message: String) {
        toastMessage = message
        showToast = true
        Task {
            try? await Task.sleep(nanoseconds: 2_500_000_000)
            showToast = false
        }
    }
}
