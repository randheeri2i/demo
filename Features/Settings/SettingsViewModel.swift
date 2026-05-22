import Foundation
import Combine

@MainActor
final class SettingsViewModel: ObservableObject {
    @Published var pushEnabled = true
    @Published var matchAlerts = true
    @Published var emailDaily = false

    let versionText = "Version 2.0.4 — PinMyHome"
}
