import SwiftUI

struct MyNetworkView: View {
    @StateObject private var viewModel = MyNetworkViewModel()
    @State private var expandedBrokerIDs: Set<Int> = []

    var onNavigateToProperty: (Int) -> Void = { _ in }
    var onBack: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            header
            bodyContent
        }
        .background(Color.pageBackground)
        .task {
            await viewModel.load()
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Button(action: onBack) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)

                VStack(alignment: .leading, spacing: 2) {
                    Text("My Network")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                    Text("Sub-brokers in your team")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.6))
                }

                Spacer()

                HStack(spacing: 5) {
                    Image(systemName: "person.2.fill")
                        .font(.system(size: 13))
                    Text("\(viewModel.subBrokers.count) Members")
                        .font(.system(size: 13, weight: .medium))
                }
                .foregroundColor(.white)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(Color.white.opacity(0.12))
                .clipShape(Capsule())
            }

            HStack(spacing: 8) {
                Text("\(viewModel.activeMembers) Active")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 6)
                    .background(Color(hex: 0xFF7A2F))
                    .clipShape(Capsule())

                Text("\(viewModel.totalProperties) Properties")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 6)
                    .background(Color(hex: 0x26B89A))
                    .clipShape(Capsule())
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.darkNavy)
    }

    @ViewBuilder
    private var bodyContent: some View {
        if viewModel.isLoading {
            Spacer()
            ProgressView().tint(Color(hex: 0x26B89A))
            Spacer()
        } else if let errorMessage = viewModel.errorMessage {
            VStack(spacing: 14) {
                Text(errorMessage)
                    .font(.system(size: 14))
                    .foregroundColor(.red)
                Button("Retry") {
                    Task { await viewModel.load() }
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 9)
                .foregroundColor(.white)
                .background(Color(hex: 0x26B89A))
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(20)
        } else {
            ScrollView {
                LazyVStack(spacing: 16) {
                    HStack(spacing: 10) {
                        NetworkStatCard(
                            value: "\(viewModel.subBrokers.count)",
                            label: "Sub-Brokers",
                            subtitle: "In your team",
                            valueColor: .textPrimary
                        )
                        NetworkStatCard(
                            value: "\(viewModel.totalProperties)",
                            label: "Properties",
                            subtitle: "By your team",
                            valueColor: Color(hex: 0x26B89A)
                        )
                        NetworkStatCard(
                            value: "\(viewModel.activeMembers)",
                            label: "Active",
                            subtitle: "Currently active",
                            valueColor: Color(hex: 0xFF7A2F)
                        )
                    }

                    Text("TEAM MEMBERS")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(.textTertiary)
                        .tracking(1.2)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    ForEach(viewModel.subBrokers) { broker in
                        BrokerCard(
                            broker: broker,
                            expanded: expandedBrokerIDs.contains(broker.id),
                            onToggle: {
                                toggleExpand(for: broker.id)
                            },
                            onNavigateToProperty: onNavigateToProperty
                        )
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 20)
            }
        }
    }

    private func toggleExpand(for brokerID: Int) {
        if expandedBrokerIDs.contains(brokerID) {
            expandedBrokerIDs.remove(brokerID)
        } else {
            expandedBrokerIDs.insert(brokerID)
        }
    }
}

private struct NetworkStatCard: View {
    let value: String
    let label: String
    let subtitle: String
    let valueColor: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(value)
                .font(.system(size: 28, weight: .bold))
                .foregroundColor(valueColor)
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(.textPrimary)
            Text(subtitle)
                .font(.system(size: 11))
                .foregroundColor(.textTertiary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .shadow(color: .black.opacity(0.05), radius: 5, y: 1)
    }
}

private struct BrokerCard: View {
    let broker: SubBroker
    let expanded: Bool
    let onToggle: () -> Void
    let onNavigateToProperty: (Int) -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .top, spacing: 14) {
                Circle()
                    .fill(Color(hex: 0x243354))
                    .frame(width: 48, height: 48)
                    .overlay(
                        Text(initials(for: broker.name))
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                    )

                VStack(alignment: .leading, spacing: 7) {
                    Text(broker.name)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.textPrimary)
                    HStack(spacing: 4) {
                        Image(systemName: "phone.fill")
                            .font(.system(size: 12))
                            .foregroundColor(.textTertiary)
                        Text(broker.phone)
                            .font(.system(size: 13))
                            .foregroundColor(.textSecondary)
                    }

                    HStack(spacing: 7) {
                        if broker.isActive {
                            ActiveChip()
                        }
                        StatusChip(label: broker.kycStatus.capitalized)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                VStack(alignment: .trailing, spacing: 8) {
                    Text("\(broker.propertiesCount)")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(Color(hex: 0x26B89A))
                    Text("Properties")
                        .font(.system(size: 11))
                        .foregroundColor(.textTertiary)

                    Button(action: onToggle) {
                        HStack(spacing: 4) {
                            Image(systemName: expanded ? "chevron.up" : "chevron.down")
                                .font(.system(size: 12, weight: .bold))
                            Text(expanded ? "Hide" : "View")
                                .font(.system(size: 12, weight: .semibold))
                        }
                        .foregroundColor(expanded ? .white : .textSecondary)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(expanded ? Color.darkNavy : Color(hex: 0xEEF2F7))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(16)

            if expanded {
                Divider()
                    .padding(.horizontal, 16)
                VStack(alignment: .leading, spacing: 10) {
                    Text("RECENT PROPERTIES")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(.textTertiary)
                        .tracking(1.0)

                    ForEach(Array(broker.recentProperties.enumerated()), id: \.element.id) { index, property in
                        if index > 0 {
                            Spacer().frame(height: 2)
                        }
                        NetworkPropertyRow(property: property) {
                            onNavigateToProperty(property.id)
                        }
                    }
                }
                .padding(16)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 5, y: 1)
        .animation(.easeInOut(duration: 0.2), value: expanded)
    }

    private func initials(for name: String) -> String {
        let letters = name.split(separator: " ").prefix(2).compactMap { $0.first?.uppercased() }
        return letters.joined()
    }
}

private struct NetworkPropertyRow: View {
    let property: NetworkProperty
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 12) {
                RoundedRectangle(cornerRadius: 10)
                    .fill(Color(hex: 0xEEF2F7))
                    .frame(width: 40, height: 40)
                    .overlay(
                        Image(systemName: "building.2")
                            .font(.system(size: 16))
                            .foregroundColor(.textSecondary)
                    )

                VStack(alignment: .leading, spacing: 2) {
                    Text(property.displayName)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.textPrimary)
                        .lineLimit(1)
                    HStack(spacing: 3) {
                        Image(systemName: "location.fill")
                            .font(.system(size: 11))
                            .foregroundColor(.textTertiary)
                        Text("\(property.location) · \(property.type)")
                            .font(.system(size: 12))
                            .foregroundColor(.textTertiary)
                            .lineLimit(1)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                VStack(alignment: .trailing, spacing: 4) {
                    Text(property.price)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.textPrimary)
                    Text("Quote")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(.textSecondary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color(hex: 0xEEF4F7))
                        .clipShape(RoundedRectangle(cornerRadius: 6))
                }
            }
            .padding(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.dividerColor, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

private struct ActiveChip: View {
    var body: some View {
        HStack(spacing: 5) {
            Circle()
                .fill(Color(hex: 0x22A06B))
                .frame(width: 7, height: 7)
            Text("Active")
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(Color(hex: 0x22A06B))
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background(Color(hex: 0xE6F7F1))
        .clipShape(Capsule())
    }
}

private struct StatusChip: View {
    let label: String

    var body: some View {
        let style: (bg: Color, fg: Color) = {
            switch label.lowercased() {
            case "verified":
                return (Color(hex: 0xE6F7F1), Color(hex: 0x26B89A))
            case "pending":
                return (Color(hex: 0xFFF3E0), Color(hex: 0xFF7A2F))
            default:
                return (Color(hex: 0xEEF2F7), .textSecondary)
            }
        }()

        return Text(label)
            .font(.system(size: 12, weight: .semibold))
            .foregroundColor(style.fg)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(style.bg)
            .clipShape(Capsule())
    }
}

#Preview {
    MyNetworkView()
}
