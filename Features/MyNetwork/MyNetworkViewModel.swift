import Foundation
import Combine

struct NetworkProperty: Identifiable, Hashable {
    let id: Int
    let societyName: String
    let tower: String?
    let flatNumber: String?
    let bhkType: String?
    let priceDemanded: Int64?
    let status: String?
    let areaName: String?

    var displayName: String {
        var parts = [societyName]
        if let tower, !tower.isEmpty { parts.append(tower) }
        if let flatNumber, !flatNumber.isEmpty { parts.append(flatNumber) }
        return parts.joined(separator: " · ")
    }

    var location: String { areaName ?? "" }
    var type: String { bhkType ?? "" }

    var price: String {
        guard let priceDemanded else { return "Price on request" }
        return String(format: "₹ %.2f Cr", Double(priceDemanded) / 10_000_000.0)
    }
}

struct SubBroker: Identifiable, Hashable {
    let id: Int
    let userID: Int
    let name: String
    let phone: String
    let kycStatus: String
    let isActive: Bool
    let createdAt: String
    let propertiesCount: Int
    let recentProperties: [NetworkProperty]
}

protocol MyNetworkServiceProtocol {
    func fetchSubBrokers() async throws -> [SubBroker]
}

final class MyNetworkMockService: MyNetworkServiceProtocol {
    func fetchSubBrokers() async throws -> [SubBroker] {
        try await Task.sleep(nanoseconds: 220_000_000)

        return [
            SubBroker(
                id: 1,
                userID: 501,
                name: "Nikhil Rao",
                phone: "9876543210",
                kycStatus: "verified",
                isActive: true,
                createdAt: "2026-05-22",
                propertiesCount: 4,
                recentProperties: [
                    NetworkProperty(
                        id: 1001,
                        societyName: "Skyline Residency",
                        tower: "B",
                        flatNumber: "1204",
                        bhkType: "3BHK",
                        priceDemanded: 18_500_000,
                        status: "quote",
                        areaName: "Whitefield"
                    ),
                    NetworkProperty(
                        id: 1002,
                        societyName: "Palm Meadows",
                        tower: "A",
                        flatNumber: "602",
                        bhkType: "2BHK",
                        priceDemanded: 9_600_000,
                        status: "offer",
                        areaName: "Sarjapur"
                    )
                )
            ),
            SubBroker(
                id: 2,
                userID: 502,
                name: "Asha Patel",
                phone: "9123456789",
                kycStatus: "pending",
                isActive: false,
                createdAt: "2026-05-20",
                propertiesCount: 2,
                recentProperties: [
                    NetworkProperty(
                        id: 1101,
                        societyName: "Green Avenue",
                        tower: nil,
                        flatNumber: "903",
                        bhkType: "2BHK",
                        priceDemanded: 8_800_000,
                        status: "quote",
                        areaName: "Bellandur"
                    )
                ]
            )
        ]
    }
}

@MainActor
final class MyNetworkViewModel: ObservableObject {
    @Published var subBrokers: [SubBroker] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let service: MyNetworkServiceProtocol

    init(service: MyNetworkServiceProtocol = MyNetworkMockService()) {
        self.service = service
    }

    func load() async {
        isLoading = true
        errorMessage = nil
        do {
            subBrokers = try await service.fetchSubBrokers()
        } catch {
            errorMessage = "Unable to load your network right now."
        }
        isLoading = false
    }

    var activeMembers: Int {
        subBrokers.filter(\.isActive).count
    }

    var totalProperties: Int {
        subBrokers.reduce(0) { $0 + $1.propertiesCount }
    }
}
