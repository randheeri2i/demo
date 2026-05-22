import SwiftUI

extension Color {
    static let brandTeal = Color(hex: 0x2EC4A5)
    static let brandTealDark = Color(hex: 0x1A9E85)
    static let brandOrange = Color(hex: 0xF5A623)

    static let darkNavy = Color(hex: 0x1A2D44)
    static let darkNavyMid = Color(hex: 0x1E3450)
    static let darkNavyDeep = Color(hex: 0x162840)
    static let navyMedium = Color(hex: 0x243459)

    static let pageBackground = Color(hex: 0xF5F6FA)
    static let cardBackground = Color(hex: 0xFFFFFF)
    static let dividerColor = Color(hex: 0xE8ECF4)

    static let textPrimary = Color(hex: 0x1B2A4A)
    static let textSecondary = Color(hex: 0x6B7A99)
    static let textTertiary = Color(hex: 0x9BA8BF)
}

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}
