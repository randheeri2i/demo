import Foundation
import Combine

struct PropertyMatchItem: Identifiable, Hashable {
    let id: String
    let propertyID: String
    let propertyName: String
    let propertyPrice: String
    let propertyLocation: String
    let buyerName: String
    let buyerBudgetMin: String
    let buyerBudgetMax: String
    let matchScore: Int
}

protocol MatchesServiceProtocol {
    func fetchMatches() async throws -> [PropertyMatchItem]
    func generateMatches() async throws
}

final class MatchesMockService: MatchesServiceProtocol {
    private var items: [PropertyMatchItem] = [
        PropertyMatchItem(
            id: "1",
            propertyID: "1001",
            propertyName: "Skyline Residency",
            propertyPrice: "₹ 1.85 Cr",
            propertyLocation: "Whitefield",
            buyerName: "Riya Shah",
            buyerBudgetMin: "1.70 Cr",
            buyerBudgetMax: "2.00 Cr",
            matchScore: 92
        ),
        PropertyMatchItem(
            id: "2",
            propertyID: "1002",
            propertyName: "Palm Meadows",
            propertyPrice: "₹ 0.96 Cr",
            propertyLocation: "Sarjapur",
            buyerName: "Karan Nair",
            buyerBudgetMin: "0.90 Cr",
            buyerBudgetMax: "1.20 Cr",
            matchScore: 78
        )
    ]

    func fetchMatches() async throws -> [PropertyMatchItem] {
        try await Task.sleep(nanoseconds: 250_000_000)
        return items
    }

    func generateMatches() async throws {
        try await Task.sleep(nanoseconds: 300_000_000)
        items.shuffle()
    }
}

@MainActor
final class MatchesViewModel: ObservableObject {
    @Published var matches: [PropertyMatchItem] = []
    @Published var isLoading = false
    @Published var isGenerating = false
    @Published var errorMessage: String?

    private let service: MatchesServiceProtocol

    init(service: MatchesServiceProtocol = MatchesMockService()) {
        self.service = service
    }

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            matches = try await service.fetchMatches()
        } catch {
            errorMessage = "Unable to load matches."
        }
        isLoading = false
    }

    func generateNewMatches() async {
        isGenerating = true
        errorMessage = nil
        do {
            try await service.generateMatches()
            matches = try await service.fetchMatches()
        } catch {
            errorMessage = "Failed to generate new matches."
        }
        isGenerating = false
    }
}
