import Foundation

enum VisitStatus: String, CaseIterable {
    case scheduled
    case completed
    case cancelled
}

struct VisitItem: Identifiable, Hashable {
    let id: String
    let propertyTitle: String
    let buyerName: String
    let buyerPhone: String
    let timeLabel: String
    let dateForStrip: Date
    var status: VisitStatus
    let visitDateTime: Date
}

struct VisitScheduleDraft {
    var propertyName = ""
    var buyerName = ""
    var buyerPhone = ""
    var dateTime = Date().addingTimeInterval(3 * 3600)
}

protocol VisitsServiceProtocol {
    func fetchVisits() async throws -> [VisitItem]
    func markVisit(id: String, status: VisitStatus) async throws
    func scheduleVisit(_ draft: VisitScheduleDraft) async throws
    func propertySuggestions() async throws -> [String]
}

final class VisitsMockService: VisitsServiceProtocol {
    private var cache: [VisitItem] = []

    init() {
        let calendar = Calendar.current
        let now = Date()

        func makeDate(dayOffset: Int, hour: Int, minute: Int) -> Date {
            let start = calendar.startOfDay(for: now)
            let day = calendar.date(byAdding: .day, value: dayOffset, to: start) ?? now
            return calendar.date(bySettingHour: hour, minute: minute, second: 0, of: day) ?? day
        }

        let first = makeDate(dayOffset: 0, hour: 11, minute: 0)
        let second = makeDate(dayOffset: 0, hour: 17, minute: 30)
        let third = makeDate(dayOffset: -1, hour: 10, minute: 30)

        cache = [
            VisitItem(
                id: "v1",
                propertyTitle: "Skyline Residency, Whitefield",
                buyerName: "Riya Shah",
                buyerPhone: "9876543210",
                timeLabel: Self.timeLabel(from: first),
                dateForStrip: first,
                status: .scheduled,
                visitDateTime: first
            ),
            VisitItem(
                id: "v2",
                propertyTitle: "Palm Meadows, Sarjapur",
                buyerName: "Karan Nair",
                buyerPhone: "9123456789",
                timeLabel: Self.timeLabel(from: second),
                dateForStrip: second,
                status: .scheduled,
                visitDateTime: second
            ),
            VisitItem(
                id: "v3",
                propertyTitle: "Green Avenue, Bellandur",
                buyerName: "Sana Mehta",
                buyerPhone: "9988776655",
                timeLabel: Self.timeLabel(from: third),
                dateForStrip: third,
                status: .completed,
                visitDateTime: third
            )
        ]
    }

    func fetchVisits() async throws -> [VisitItem] {
        try await Task.sleep(nanoseconds: 200_000_000)
        return cache.sorted { $0.visitDateTime > $1.visitDateTime }
    }

    func markVisit(id: String, status: VisitStatus) async throws {
        try await Task.sleep(nanoseconds: 120_000_000)
        if let idx = cache.firstIndex(where: { $0.id == id }) {
            cache[idx].status = status
        }
    }

    func scheduleVisit(_ draft: VisitScheduleDraft) async throws {
        try await Task.sleep(nanoseconds: 180_000_000)
        let new = VisitItem(
            id: UUID().uuidString,
            propertyTitle: draft.propertyName,
            buyerName: draft.buyerName,
            buyerPhone: draft.buyerPhone,
            timeLabel: Self.timeLabel(from: draft.dateTime),
            dateForStrip: draft.dateTime,
            status: .scheduled,
            visitDateTime: draft.dateTime
        )
        cache.insert(new, at: 0)
    }

    func propertySuggestions() async throws -> [String] {
        [
            "Skyline Residency, Whitefield",
            "Palm Meadows, Sarjapur",
            "Green Avenue, Bellandur",
            "Lake View Heights, HSR"
        ]
    }

    private static func timeLabel(from date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEE, d MMM · hh:mm a"
        return formatter.string(from: date)
    }
}

@MainActor
final class VisitsViewModel: ObservableObject {
    @Published var visits: [VisitItem] = []
    @Published var selectedDate = Date()
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var isSaving = false
    @Published var scheduleError: String?
    @Published var propertyOptions: [String] = []

    private let service: VisitsServiceProtocol
    private let calendar = Calendar.current

    init(service: VisitsServiceProtocol = VisitsMockService()) {
        self.service = service
    }

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            async let visitsTask = service.fetchVisits()
            async let propertiesTask = service.propertySuggestions()
            visits = try await visitsTask
            propertyOptions = try await propertiesTask
        } catch {
            errorMessage = "Unable to load visits."
        }
        isLoading = false
    }

    func markComplete(id: String) async {
        await markVisit(id: id, as: .completed)
    }

    func cancel(id: String) async {
        await markVisit(id: id, as: .cancelled)
    }

    private func markVisit(id: String, as status: VisitStatus) async {
        do {
            try await service.markVisit(id: id, status: status)
            visits = try await service.fetchVisits()
        } catch {
            errorMessage = "Unable to update visit status."
        }
    }

    func schedule(_ draft: VisitScheduleDraft) async -> Bool {
        isSaving = true
        scheduleError = nil

        do {
            try await service.scheduleVisit(draft)
            visits = try await service.fetchVisits()
            isSaving = false
            return true
        } catch {
            scheduleError = "Unable to schedule this visit."
            isSaving = false
            return false
        }
    }

    var todayCount: Int {
        visits.filter { calendar.isDateInToday($0.dateForStrip) }.count
    }

    var doneCount: Int {
        visits.filter { $0.status == .completed }.count
    }

    var cancelledCount: Int {
        visits.filter { $0.status == .cancelled }.count
    }

    var selectedVisits: [VisitItem] {
        visits.filter { calendar.isDate($0.dateForStrip, inSameDayAs: selectedDate) }
    }

    var earlierVisits: [VisitItem] {
        let today = calendar.startOfDay(for: Date())
        guard calendar.isDate(selectedDate, inSameDayAs: today) else { return [] }

        return visits.filter { calendar.startOfDay(for: $0.dateForStrip) < today }
    }
}
