import Foundation

struct DemandItem: Identifiable, Hashable {
    let id: String
    let buyerName: String
    let buyerPhone: String
    let flatType: String
    let budgetMin: Int64
    let budgetMax: Int64
    let areaID: Int?
    let areaName: String
    let isExclusive: Bool
    let createdAt: String
    let matchingCount: Int

    var budgetDisplay: String {
        "\(Self.format(rupees: budgetMin)) – \(Self.format(rupees: budgetMax))"
    }

    private static func format(rupees: Int64) -> String {
        let crore = Double(rupees) / 10_000_000.0
        let lakh = Double(rupees) / 100_000.0

        if crore >= 1 {
            return String(format: "₹%.2f Cr", crore)
        }

        if lakh >= 1 {
            return String(format: "₹%.1f L", lakh)
        }

        return "₹\(rupees)"
    }
}

struct BuyerDemandDraft {
    var name = ""
    var phone = ""
    var minBudget = ""
    var maxBudget = ""
    var flatType = ""
    var area = ""
    var isExclusive = false
    var areaID: Int?
}

struct AreaOption: Identifiable, Hashable {
    let id: Int
    let name: String
    let city: String
}

protocol DemandsServiceProtocol {
    func fetchDemands() async throws -> [DemandItem]
    func createDemand(_ draft: BuyerDemandDraft) async throws
    func fetchAreas() async throws -> [AreaOption]
}

final class DemandsMockService: DemandsServiceProtocol {
    func fetchDemands() async throws -> [DemandItem] {
        try await Task.sleep(nanoseconds: 250_000_000)
        return [
            DemandItem(
                id: "1",
                buyerName: "Riya Shah",
                buyerPhone: "9876543210",
                flatType: "3BHK",
                budgetMin: 12_000_000,
                budgetMax: 16_000_000,
                areaID: 11,
                areaName: "Whitefield",
                isExclusive: true,
                createdAt: "2026-05-22",
                matchingCount: 4
            ),
            DemandItem(
                id: "2",
                buyerName: "Karan Nair",
                buyerPhone: "9123456789",
                flatType: "2BHK",
                budgetMin: 7_500_000,
                budgetMax: 11_000_000,
                areaID: 8,
                areaName: "Sarjapur",
                isExclusive: false,
                createdAt: "2026-05-18",
                matchingCount: 1
            )
        ]
    }

    func createDemand(_ draft: BuyerDemandDraft) async throws {
        _ = draft
        try await Task.sleep(nanoseconds: 220_000_000)
    }

    func fetchAreas() async throws -> [AreaOption] {
        [
            AreaOption(id: 1, name: "Whitefield", city: "Bengaluru"),
            AreaOption(id: 2, name: "Sarjapur", city: "Bengaluru"),
            AreaOption(id: 3, name: "Electronic City", city: "Bengaluru")
        ]
    }
}

@MainActor
final class DemandsViewModel: ObservableObject {
    @Published var demands: [DemandItem] = []
    @Published var searchQuery = ""
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var saveErrorMessage: String?
    @Published var isSaving = false
    @Published var areas: [AreaOption] = []

    private let service: DemandsServiceProtocol

    init(service: DemandsServiceProtocol = DemandsMockService()) {
        self.service = service
    }

    var filteredDemands: [DemandItem] {
        guard !searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return demands
        }

        return demands.filter {
            $0.buyerName.localizedCaseInsensitiveContains(searchQuery) ||
            $0.areaName.localizedCaseInsensitiveContains(searchQuery) ||
            $0.flatType.localizedCaseInsensitiveContains(searchQuery)
        }
    }

    var exclusiveCount: Int {
        demands.filter(\.isExclusive).count
    }

    var matchedCount: Int {
        demands.reduce(0) { $0 + $1.matchingCount }
    }

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            async let demandTask = service.fetchDemands()
            async let areaTask = service.fetchAreas()
            demands = try await demandTask
            areas = try await areaTask
        } catch {
            errorMessage = "Unable to load demands."
        }
        isLoading = false
    }

    func createDemand(_ draft: BuyerDemandDraft) async -> Bool {
        isSaving = true
        saveErrorMessage = nil

        do {
            try await service.createDemand(draft)
            demands = try await service.fetchDemands()
            isSaving = false
            return true
        } catch {
            saveErrorMessage = "Failed to save demand."
            isSaving = false
            return false
        }
    }
}
