import Foundation
import SwiftUI
import Combine

struct PropertyListing: Identifiable, Hashable {
    let id: String
    let name: String
    let location: String
    let tower: String
    let flatNumber: String
    let floor: String
    let bhk: String
    let sqft: String
    let price: String
    let priceRaw: Int64
    let finalPrice: String
    let pricePerSqft: String
    let commission: String
    let furnished: String
    let status: String
    let facing: String
    let sunlight: String
    let parking: String
    let possession: String
    let sellerName: String
    let sellerPhone: String
    let brokerName: String
    let brokerRole: String
    let listedDate: String
    let description: String
    let amenities: [String]
    let images: [String]
    let primaryPhoto: String?
    let photoCount: Int
    let societyId: Int
    let areaId: Int
    let constructionYear: String
    let videoLink: String
    let brochureURL: String
}

protocol PropertyServiceProtocol {
    func fetchProperties(status: String?) async throws -> [PropertyListing]
}

enum PropertyScreenError: Error {
    case network(String)
}

final class PropertyMockService: PropertyServiceProtocol {
    func fetchProperties(status: String?) async throws -> [PropertyListing] {
        try await Task.sleep(nanoseconds: 250_000_000)

        let sample = [
            PropertyListing(
                id: "101",
                name: "Skyline Residency",
                location: "Whitefield",
                tower: "B",
                flatNumber: "1204",
                floor: "Floor 12",
                bhk: "3BHK",
                sqft: "1680 sqft",
                price: "₹1.85 Cr",
                priceRaw: 18_500_000,
                finalPrice: "₹1.79 Cr",
                pricePerSqft: "₹11,011/sqft",
                commission: "2.0%",
                furnished: "Semi Furnished",
                status: "Listed",
                facing: "East",
                sunlight: "Good",
                parking: "2",
                possession: "Ready To Move",
                sellerName: "Rohan Verma",
                sellerPhone: "9876543210",
                brokerName: "Alex Johnson",
                brokerRole: "child_broker",
                listedDate: "2026-05-20",
                description: "Corner apartment with open view.",
                amenities: ["Gym", "Pool", "Clubhouse"],
                images: ["https://images.unsplash.com/photo-1523217582562-09d0def993a6?q=80&w=2000&auto=format&fit=crop"],
                primaryPhoto: "https://images.unsplash.com/photo-1523217582562-09d0def993a6?q=80&w=2000&auto=format&fit=crop",
                photoCount: 8,
                societyId: 1,
                areaId: 11,
                constructionYear: "2019",
                videoLink: "",
                brochureURL: ""
            ),
            PropertyListing(
                id: "102",
                name: "Palm Meadows",
                location: "Sarjapur Road",
                tower: "A",
                flatNumber: "602",
                floor: "Floor 6",
                bhk: "2BHK",
                sqft: "1245 sqft",
                price: "₹96.0 L",
                priceRaw: 9_600_000,
                finalPrice: "",
                pricePerSqft: "₹7,710/sqft",
                commission: "1.5%",
                furnished: "Unfurnished",
                status: "Inspection",
                facing: "North",
                sunlight: "Medium",
                parking: "1",
                possession: "Under Construction",
                sellerName: "Pooja Shah",
                sellerPhone: "9123456789",
                brokerName: "Alex Johnson",
                brokerRole: "child_broker",
                listedDate: "2026-05-19",
                description: "",
                amenities: ["Park", "Jogging Track"],
                images: [],
                primaryPhoto: nil,
                photoCount: 0,
                societyId: 2,
                areaId: 8,
                constructionYear: "2027",
                videoLink: "",
                brochureURL: ""
            )
        ]

        guard let status, !status.isEmpty else {
            return sample
        }

        return sample.filter { $0.status.lowercased() == status.lowercased() }
    }
}

@MainActor
final class PropertyViewModel: ObservableObject {
    @Published var searchQuery = ""
    @Published var selectedStatus = "All"
    @Published private(set) var properties: [PropertyListing] = []
    @Published private(set) var isLoading = false
    @Published var errorMessage: String?

    let statusFilters = ["All", "Listed", "Inspection", "Acquired", "Visits", "Bba", "Sold", "Quote", "Offer"]

    private let service: PropertyServiceProtocol

    init(service: PropertyServiceProtocol = PropertyMockService()) {
        self.service = service
    }

    func loadProperties() async {
        isLoading = true
        errorMessage = nil

        do {
            let status = selectedStatus == "All" ? nil : selectedStatus.lowercased()
            properties = try await service.fetchProperties(status: status)
        } catch {
            errorMessage = "Unable to load properties right now."
        }

        isLoading = false
    }

    var filteredProperties: [PropertyListing] {
        let query = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else { return properties }

        return properties.filter { property in
            property.name.localizedCaseInsensitiveContains(query) ||
            property.location.localizedCaseInsensitiveContains(query) ||
            property.tower.localizedCaseInsensitiveContains(query) ||
            property.flatNumber.localizedCaseInsensitiveContains(query) ||
            property.bhk.localizedCaseInsensitiveContains(query)
        }
    }

    func statusColors(for status: String) -> (bg: Color, text: Color) {
        switch status.lowercased() {
        case "inspection":
            return (Color(hex: 0xFFF3CD), Color(hex: 0x856404))
        case "listed":
            return (Color(hex: 0xD1F2EB), Color(hex: 0x0E6655))
        case "visits":
            return (Color(hex: 0xFFEAA7), Color(hex: 0x8D6708))
        case "bba", "acquired":
            return (Color(hex: 0xD4EDDA), Color(hex: 0x155724))
        case "quote":
            return (Color(hex: 0xCCE5FF), Color(hex: 0x004085))
        case "offer":
            return (Color(hex: 0xFFF3CD), Color(hex: 0x856404))
        case "sold":
            return (Color(hex: 0xF8D7DA), Color(hex: 0x721C24))
        default:
            return (Color(hex: 0xE2E8F0), .textSecondary)
        }
    }
}
