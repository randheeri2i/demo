import Foundation

struct ProfileUiState: Hashable {
    var fullName = ""
    var phoneNumber = ""
    var email = ""
    var role = ""
    var memberSince = ""
    var brokerID = ""
    var kycStatus = ""
    var panNumber = ""
    var accountType = ""
    var isIndependent = true
}

protocol ProfileServiceProtocol {
    func getProfile() async throws -> ProfileUiState
    func updatePersonal(name: String, email: String) async throws
    func updatePan(_ pan: String) async throws
    func generateInvite(name: String, phone: String) async throws -> String
    func latestInvite() async throws -> String?
    func subBrokerCount() async throws -> Int
}

final class ProfileMockService: ProfileServiceProtocol {
    private var currentInvite: String?

    func getProfile() async throws -> ProfileUiState {
        try await Task.sleep(nanoseconds: 250_000_000)
        return ProfileUiState(
            fullName: "Raghavendra S",
            phoneNumber: "9876543210",
            email: "raghav@example.com",
            role: "Child Broker",
            memberSince: "May 22, 2025",
            brokerID: "#117",
            kycStatus: "Verified",
            panNumber: "ABCDE1234F",
            accountType: "Sub-Broker",
            isIndependent: false
        )
    }

    func updatePersonal(name: String, email: String) async throws {
        _ = (name, email)
        try await Task.sleep(nanoseconds: 140_000_000)
    }

    func updatePan(_ pan: String) async throws {
        _ = pan
        try await Task.sleep(nanoseconds: 140_000_000)
    }

    func generateInvite(name: String, phone: String) async throws -> String {
        _ = (name, phone)
        try await Task.sleep(nanoseconds: 220_000_000)
        let url = "https://pinmyhome.app/invite/\(UUID().uuidString.prefix(8))"
        currentInvite = url
        return url
    }

    func latestInvite() async throws -> String? {
        currentInvite
    }

    func subBrokerCount() async throws -> Int {
        6
    }
}

@MainActor
final class ProfileViewModel: ObservableObject {
    @Published var profile = ProfileUiState()
    @Published var isLoading = false
    @Published var loadError: String?
    @Published var saveError: String?
    @Published var isSaving = false

    @Published var inviteURL: String?
    @Published var subBrokerCount = 0
    @Published var inviteError: String?
    @Published var copiedInvite = false

    private let service: ProfileServiceProtocol

    init(service: ProfileServiceProtocol = ProfileMockService()) {
        self.service = service
    }

    func load() async {
        isLoading = true
        loadError = nil
        do {
            async let profileTask = service.getProfile()
            async let inviteTask = service.latestInvite()
            async let countTask = service.subBrokerCount()
            profile = try await profileTask
            inviteURL = try await inviteTask
            subBrokerCount = try await countTask
        } catch {
            loadError = "Unable to load profile details."
        }
        isLoading = false
    }

    func savePersonal(name: String, email: String) async -> Bool {
        isSaving = true
        saveError = nil
        do {
            try await service.updatePersonal(name: name, email: email)
            profile.fullName = name
            profile.email = email
            isSaving = false
            return true
        } catch {
            saveError = "Unable to update personal details."
            isSaving = false
            return false
        }
    }

    func savePan(_ pan: String) async -> Bool {
        isSaving = true
        saveError = nil
        do {
            try await service.updatePan(pan)
            profile.panNumber = pan
            isSaving = false
            return true
        } catch {
            saveError = "Unable to update PAN number."
            isSaving = false
            return false
        }
    }

    func generateInvite(name: String, phone: String) async -> String? {
        inviteError = nil
        copiedInvite = false
        do {
            let url = try await service.generateInvite(name: name, phone: phone)
            inviteURL = url
            return url
        } catch {
            inviteError = "Failed to generate invite link."
            return nil
        }
    }
}
