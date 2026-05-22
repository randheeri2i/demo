import SwiftUI

struct SettingsView: View {
    @StateObject private var viewModel = SettingsViewModel()

    var onBack: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            header

            ScrollView {
                VStack(spacing: 16) {
                    SettingsGroupCard(title: "Real Estate Alerts") {
                        ToggleRow(
                            title: "Property Match Alerts",
                            subtitle: "Notify when new leads match your area",
                            icon: "building.2",
                            checked: $viewModel.matchAlerts
                        )
                        SettingsDivider()
                        ToggleRow(
                            title: "Push Notifications",
                            subtitle: "Daily activity and visit updates",
                            icon: "bell.badge",
                            checked: $viewModel.pushEnabled
                        )
                        SettingsDivider()
                        ToggleRow(
                            title: "Email Summary",
                            subtitle: "Weekly performance report",
                            icon: "envelope",
                            checked: $viewModel.emailDaily
                        )
                    }

                    SettingsGroupCard(title: "Security & Privacy") {
                        NavigationRow(title: "Change Password", icon: "lock") {}
                        SettingsDivider()
                        NavigationRow(title: "Privacy Settings", icon: "checkmark.shield") {}
                    }

                    SettingsGroupCard(title: "Support") {
                        NavigationRow(title: "Help Center", icon: "questionmark.circle") {}
                        SettingsDivider()
                        NavigationRow(title: "Terms of Service", icon: "doc.text") {}
                    }

                    Text(viewModel.versionText)
                        .font(.system(size: 12))
                        .foregroundColor(.textTertiary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                }
                .padding(16)
            }
            .background(Color.pageBackground)
        }
        .background(Color.pageBackground)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)

                Text("App Settings")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
            }

            Text("Preferences & Security")
                .font(.system(size: 12))
                .foregroundColor(.white)
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(Color.white.opacity(0.12))
                .clipShape(Capsule())
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.darkNavy)
    }
}

private struct SettingsGroupCard<Content: View>: View {
    let title: String
    let content: Content

    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(Color(hex: 0x26B89A))
            content
        }
        .padding(20)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 6, y: 1)
    }
}

private struct ToggleRow: View {
    let title: String
    let subtitle: String
    let icon: String
    @Binding var checked: Bool

    var body: some View {
        HStack(spacing: 14) {
            Circle()
                .fill(Color(hex: 0xEEF4F7))
                .frame(width: 40, height: 40)
                .overlay(
                    Image(systemName: icon)
                        .font(.system(size: 18))
                        .foregroundColor(.darkNavy)
                )

            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.textPrimary)
                Text(subtitle)
                    .font(.system(size: 12))
                    .foregroundColor(.textTertiary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Toggle("", isOn: $checked)
                .labelsHidden()
                .tint(Color(hex: 0x26B89A))
        }
    }
}

private struct NavigationRow: View {
    let title: String
    let icon: String
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 14) {
                Circle()
                    .fill(Color(hex: 0xEEF4F7))
                    .frame(width: 40, height: 40)
                    .overlay(
                        Image(systemName: icon)
                            .font(.system(size: 18))
                            .foregroundColor(.darkNavy)
                    )

                Text(title)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color.dividerColor)
            }
        }
        .buttonStyle(.plain)
    }
}

private struct SettingsDivider: View {
    var body: some View {
        Divider()
            .overlay(Color.dividerColor)
            .padding(.leading, 54)
    }
}

#Preview {
    SettingsView()
}
