import Foundation

struct BuyerItem: Identifiable, Hashable {
    let id: Int
    let name: String
    let phone: String
    let email: String
    let flatType: String
    let area: String
    let minBudget: String
    let maxBudget: String
    let isActive: Bool
    let visitCount: Int
    let dealCount: Int
}

protocol MyBuyersServiceProtocol {
    func fetchMyBuyers() async throws -> [BuyerItem]
}

final class MyBuyersMockService: MyBuyersServiceProtocol {
    func fetchMyBuyers() async throws -> [BuyerItem] {
        try await Task.sleep(nanoseconds: 220_000_000)
        return [
            BuyerItem(
                id: 1,
                name: "Riya Shah",
                phone: "9876543210",
                email: "riya@example.com",
                flatType: "3BHK",
                area: "Whitefield",
                minBudget: "1.2",
                maxBudget: "1.8",
                isActive: true,
                visitCount: 3,
                dealCount: 1
            ),
            BuyerItem(
                id: 2,
                name: "Karan Nair",
                phone: "9123456789",
                email: "karan@example.com",
                flatType: "2BHK",
                area: "Sarjapur",
                minBudget: "0.8",
                maxBudget: "1.2",
                isActive: false,
                visitCount: 1,
                dealCount: 0
            )
        ]
    }
}

@MainActor
final class MyBuyersViewModel: ObservableObject {
    @Published var searchQuery = ""
    @Published var buyers: [BuyerItem] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let service: MyBuyersServiceProtocol

    init(service: MyBuyersServiceProtocol = MyBuyersMockService()) {
        self.service = service
    }

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            buyers = try await service.fetchMyBuyers()
        } catch {
            errorMessage = "Unable to load buyers."
        }
        isLoading = false
    }

    var filtered: [BuyerItem] {
        guard !searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return buyers
        }

        return buyers.filter {
            $0.name.localizedCaseInsensitiveContains(searchQuery) ||
            $0.area.localizedCaseInsensitiveContains(searchQuery)
        }
    }

    var totalCount: Int { buyers.count }
    var activeCount: Int { buyers.filter(\.isActive).count }
    var closedCount: Int { buyers.filter { !$0.isActive }.count }
}
