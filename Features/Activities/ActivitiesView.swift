import SwiftUI

struct ActivitiesView: View {
    @StateObject private var viewModel = ActivitiesViewModel()
    @State private var showScheduleSheet = false

    var onBack: () -> Void = {}

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 0) {
                header
                content
            }
            .background(Color.pageBackground)
            .task {
                await viewModel.load()
            }

            if viewModel.showToast, let toastMessage = viewModel.toastMessage {
                ToastView(message: toastMessage)
                    .padding(.bottom, 32)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.25), value: viewModel.showToast)
        .sheet(isPresented: $showScheduleSheet) {
            ScheduleActivitySheet(
                activityTypes: viewModel.activityTypes,
                onSchedule: { draft in
                    await viewModel.schedule(draft)
                    showScheduleSheet = false
                }
            )
            .presentationDetents([.large])
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
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

                Text("Activities Hub")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                Spacer()

                Button {
                    showScheduleSheet = true
                } label: {
                    Image(systemName: "plus")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(Color(hex: 0x26B89A))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
            }

            Text("Track Leads & Visits")
                .font(.system(size: 12))
                .foregroundColor(.white)
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(Color.white.opacity(0.12))
                .clipShape(Capsule())
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.darkNavy)
    }

    @ViewBuilder
    private var content: some View {
        if viewModel.isLoading {
            Spacer()
            ProgressView().tint(Color(hex: 0x26B89A))
            Spacer()
        } else if let errorMessage = viewModel.errorMessage {
            VStack(spacing: 12) {
                Text(errorMessage)
                    .font(.system(size: 15))
                    .foregroundColor(.textPrimary)
                Button("Retry") {
                    Task { await viewModel.load() }
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 10)
                .foregroundColor(.white)
                .background(Color(hex: 0x26B89A))
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if viewModel.activities.isEmpty {
            VStack {
                Spacer()
                Text("No activities yet")
                    .font(.system(size: 15))
                    .foregroundColor(.textTertiary)
                Spacer()
            }
            .frame(maxWidth: .infinity)
        } else {
            ScrollView {
                LazyVStack(spacing: 14) {
                    ForEach(viewModel.activities) { activity in
                        ActivityCard(activity: activity) {
                            Task { await viewModel.markComplete(activity) }
                        }
                    }
                }
                .padding(16)
            }
        }
    }
}

private struct ActivityCard: View {
    let activity: ActivityFeedItem
    let onComplete: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(alignment: .top, spacing: 14) {
                Circle()
                    .fill(Color(hex: 0xEEF4F7))
                    .frame(width: 40, height: 40)
                    .overlay(
                        Image(systemName: "clock.arrow.circlepath")
                            .font(.system(size: 18))
                            .foregroundColor(.darkNavy)
                    )

                VStack(alignment: .leading, spacing: 4) {
                    Text(activity.title)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.textPrimary)
                    HStack(spacing: 4) {
                        Image(systemName: "calendar")
                            .font(.system(size: 12))
                            .foregroundColor(.textTertiary)
                        Text(activity.dateTime)
                            .font(.system(size: 12))
                            .foregroundColor(.textTertiary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                Text(activity.status == .pending ? "Pending" : "Done")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(activity.status == .pending ? .darkNavy : Color(hex: 0x26B89A))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(
                        (activity.status == .pending ? Color.darkNavy : Color(hex: 0x26B89A))
                            .opacity(0.1)
                    )
                    .clipShape(Capsule())
            }

            if activity.status == .pending {
                Button(action: onComplete) {
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark")
                            .font(.system(size: 14, weight: .bold))
                        Text("Mark as Completed")
                            .font(.system(size: 13, weight: .bold))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 42)
                    .background(Color.darkNavy)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(20)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 6, y: 1)
    }
}

private struct ScheduleActivitySheet: View {
    let activityTypes: [(key: String, label: String)]
    let onSchedule: (ActivityDraft) async -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var draft = ActivityDraft()

    var body: some View {
        NavigationStack {
            Form {
                Section("Activity Type") {
                    Picker("Type", selection: $draft.type) {
                        ForEach(activityTypes, id: \.key) { item in
                            Text(item.label).tag(item.key)
                        }
                    }
                }

                Section("Description") {
                    TextField("What needs to be done?", text: $draft.description, axis: .vertical)
                        .lineLimit(2...4)
                }

                Section("Date & Time") {
                    DatePicker("Scheduled Time", selection: $draft.scheduledTime)
                }
            }
            .navigationTitle("Schedule Activity")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Confirm") {
                        Task {
                            await onSchedule(draft)
                        }
                    }
                    .disabled(draft.description.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }
}

private struct ToastView: View {
    let message: String

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 18))
                .foregroundColor(Color(hex: 0x26B89A))
            Text(message)
                .font(.system(size: 14))
                .foregroundColor(.white)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(Color.darkNavy)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.2), radius: 8, y: 2)
    }
}

#Preview {
    ActivitiesView()
}
