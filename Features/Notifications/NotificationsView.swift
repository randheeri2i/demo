import SwiftUI

struct NotificationsView: View {
    @StateObject private var viewModel = NotificationsViewModel()

    var onBackClick: () -> Void = {}

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
                Button(action: onBackClick) {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)

                Text("Notifications")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)

                Spacer()

                Button("Mark all read") {
                    Task { await viewModel.markAllRead() }
                }
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(.white)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(Color.white.opacity(0.15))
                .clipShape(Capsule())
            }

            HStack(spacing: 10) {
                Button {
                    viewModel.selectedTab = .unread
                } label: {
                    Text(viewModel.unreadCount > 0 ? "\(viewModel.unreadCount) unread" : "Unread")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 7)
                        .background(viewModel.selectedTab == .unread ? Color(hex: 0xFF7A2F) : .clear)
                        .clipShape(Capsule())
                }
                .buttonStyle(.plain)

                Button {
                    viewModel.selectedTab = .all
                } label: {
                    Text("All")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(viewModel.selectedTab == .all ? .white : .textPrimary)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 7)
                        .background(viewModel.selectedTab == .all ? Color.white.opacity(0.22) : Color(hex: 0xCDD2DC, alpha: 0.45))
                        .clipShape(Capsule())
                }
                .buttonStyle(.plain)
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
            ProgressView().tint(Color.darkNavy)
            Spacer()
        } else if viewModel.displayedItems.isEmpty {
            VStack(spacing: 10) {
                Text("🎉")
                    .font(.system(size: 48))
                Text("You're all caught up!")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(.textPrimary)
                Text("No unread notifications.")
                    .font(.system(size: 13))
                    .foregroundColor(.textSecondary)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(32)
        } else {
            ScrollView {
                LazyVStack(spacing: 0) {
                    if !viewModel.newItems.isEmpty {
                        SectionLabel(label: "NEW")
                        ForEach(viewModel.newItems) { item in
                            NotificationCard(item: item) {
                                Task { await viewModel.markRead(item) }
                            }
                        }
                    }

                    if !viewModel.earlierItems.isEmpty {
                        if !viewModel.newItems.isEmpty {
                            Spacer().frame(height: 8)
                        }
                        SectionLabel(label: "EARLIER")
                        ForEach(viewModel.earlierItems) { item in
                            NotificationCard(item: item) {
                                Task { await viewModel.markRead(item) }
                            }
                        }
                    }
                }
                .padding(.vertical, 10)
            }
        }
    }
}

private struct SectionLabel: View {
    let label: String

    var body: some View {
        Text(label)
            .font(.system(size: 11, weight: .semibold))
            .foregroundColor(.textTertiary)
            .tracking(1.2)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
    }
}

private struct NotificationCard: View {
    let item: AppNotificationItem
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: 14) {
                RoundedRectangle(cornerRadius: 12)
                    .fill(item.iconBackground)
                    .frame(width: 46, height: 46)
                    .overlay(
                        Text(item.iconEmoji)
                            .font(.system(size: 22))
                    )

                VStack(alignment: .leading, spacing: 3) {
                    Text(item.title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.textPrimary)
                        .lineLimit(1)
                    Text(item.subtitle)
                        .font(.system(size: 13))
                        .foregroundColor(.textSecondary)
                        .lineLimit(2)
                    Text(item.timeAgo)
                        .font(.system(size: 11))
                        .foregroundColor(.textTertiary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                if item.isUnread {
                    Circle()
                        .fill(Color(hex: 0x2ECC71))
                        .frame(width: 9, height: 9)
                        .padding(.top, 5)
                }
            }
            .padding(14)
            .background(.white)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(Color.black.opacity(0.03), lineWidth: 1)
            )
            .padding(.horizontal, 14)
            .padding(.vertical, 4)
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    NotificationsView()
}
