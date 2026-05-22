import SwiftUI

struct VisitsView: View {
    @StateObject private var viewModel = VisitsViewModel()
    @State private var showScheduleDialog = false
    @State private var showCustomDatePicker = false

    private var stripDates: [Date] {
        (-2...2).compactMap { Calendar.current.date(byAdding: .day, value: $0, to: Date()) }
    }

    var body: some View {
        VStack(spacing: 0) {
            header

            if viewModel.isLoading {
                ProgressView()
                    .tint(.brandTeal)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
            }

            statsRow
            content
        }
        .background(Color.pageBackground)
        .task {
            await viewModel.load()
        }
        .sheet(isPresented: $showScheduleDialog) {
            ScheduleVisitSheet(
                propertyOptions: viewModel.propertyOptions,
                isSaving: viewModel.isSaving,
                errorMessage: viewModel.scheduleError,
                onSchedule: { draft in
                    let scheduled = await viewModel.schedule(draft)
                    if scheduled { showScheduleDialog = false }
                }
            )
            .presentationDetents([.large])
        }
        .sheet(isPresented: $showCustomDatePicker) {
            NavigationStack {
                DatePicker(
                    "Pick date",
                    selection: $viewModel.selectedDate,
                    displayedComponents: .date
                )
                .datePickerStyle(.graphical)
                .padding()
                .navigationTitle("Select Date")
                .toolbar {
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Done") { showCustomDatePicker = false }
                    }
                }
            }
            .presentationDetents([.medium])
        }
    }

    private var header: some View {
        VStack(spacing: 14) {
            HStack {
                Text("Visits")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                Button {
                    showScheduleDialog = true
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "plus")
                            .font(.system(size: 14, weight: .semibold))
                        Text("Schedule")
                            .font(.system(size: 14, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(Color(hex: 0x26B89A))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
            }

            HStack(spacing: 8) {
                ForEach(stripDates, id: \.self) { date in
                    DateCell(
                        date: date,
                        isSelected: Calendar.current.isDate(date, inSameDayAs: viewModel.selectedDate),
                        hasVisit: viewModel.visits.contains {
                            Calendar.current.isDate($0.dateForStrip, inSameDayAs: date) && $0.status == .scheduled
                        },
                        isToday: Calendar.current.isDateInToday(date)
                    ) {
                        viewModel.selectedDate = date
                    }
                }

                Button {
                    showCustomDatePicker = true
                } label: {
                    VStack(spacing: 3) {
                        Image(systemName: "calendar")
                            .font(.system(size: 17, weight: .semibold))
                        Text("Pick date")
                            .font(.system(size: 10, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity, minHeight: 72)
                    .background(Color.white.opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 16)
        .background(Color.darkNavy)
    }

    private var statsRow: some View {
        HStack(spacing: 10) {
            VisitStatCard(emoji: "📅", count: viewModel.todayCount, label: "Today")
            VisitStatCard(emoji: "✅", count: viewModel.doneCount, label: "Done")
            VisitStatCard(emoji: "⛔", count: viewModel.cancelledCount, label: "Cancelled")
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }

    private var content: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                if let error = viewModel.errorMessage {
                    HStack(spacing: 8) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.red)
                        Text(error)
                            .font(.system(size: 13))
                            .foregroundColor(.red)
                        Spacer()
                    }
                    .padding(12)
                    .background(Color.red.opacity(0.12))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .padding(.horizontal, 14)
                }

                if !viewModel.selectedVisits.isEmpty {
                    sectionLabel(for: viewModel.selectedDate)
                    ForEach(viewModel.selectedVisits) { visit in
                        VisitCard(
                            visit: visit,
                            onMarkComplete: {
                                Task { await viewModel.markComplete(id: visit.id) }
                            },
                            onCancel: {
                                Task { await viewModel.cancel(id: visit.id) }
                            }
                        )
                    }
                }

                if !viewModel.earlierVisits.isEmpty {
                    Text("EARLIER")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.textTertiary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 20)
                        .padding(.top, 12)

                    ForEach(viewModel.earlierVisits) { visit in
                        VisitCard(
                            visit: visit,
                            onMarkComplete: {
                                Task { await viewModel.markComplete(id: visit.id) }
                            },
                            onCancel: {
                                Task { await viewModel.cancel(id: visit.id) }
                            }
                        )
                    }
                }

                if viewModel.selectedVisits.isEmpty && viewModel.earlierVisits.isEmpty && !viewModel.isLoading {
                    VStack(spacing: 12) {
                        Text("🗓️")
                            .font(.system(size: 46))
                        Text("No visits for this day")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.textSecondary)
                        Text("Tap + Schedule to add one")
                            .font(.system(size: 13))
                            .foregroundColor(.textTertiary)
                    }
                    .padding(44)
                }
            }
            .padding(.bottom, 26)
        }
    }

    private func sectionLabel(for date: Date) -> some View {
        let formatter = DateFormatter()
        formatter.dateFormat = Calendar.current.isDateInToday(date) ? "'TODAY'" : "MMMM d, yyyy"
        let text = formatter.string(from: date).uppercased()

        return Text(text)
            .font(.system(size: 12, weight: .semibold))
            .foregroundColor(.textTertiary)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
            .padding(.top, 12)
    }
}

private struct DateCell: View {
    let date: Date
    let isSelected: Bool
    let hasVisit: Bool
    let isToday: Bool
    let onClick: () -> Void

    var body: some View {
        let dayFormatter = DateFormatter()
        dayFormatter.dateFormat = "EEE"
        let dayName = dayFormatter.string(from: date)

        return Button(action: onClick) {
            VStack(spacing: 4) {
                Text(isToday ? "Today" : dayName)
                    .font(.system(size: 10))
                    .foregroundColor(isSelected ? .white : .textTertiary)
                Text("\(Calendar.current.component(.day, from: date))")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                Circle()
                    .fill(hasVisit ? Color.white : .clear)
                    .frame(width: 5, height: 5)
            }
            .frame(maxWidth: .infinity, minHeight: 72)
            .background(isSelected ? Color(hex: 0xFF7A2F) : Color(hex: 0x243354))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }
}

private struct VisitStatCard: View {
    let emoji: String
    let count: Int
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            Text(emoji).font(.system(size: 22))
            Text("\(count) \(label)")
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(.textPrimary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .shadow(color: .black.opacity(0.05), radius: 6, y: 1)
    }
}

private struct VisitCard: View {
    let visit: VisitItem
    let onMarkComplete: () -> Void
    let onCancel: () -> Void

    @State private var isActing = false

    private var accentColor: Color {
        switch visit.status {
        case .scheduled: return Color(hex: 0x3B62D9)
        case .completed: return Color(hex: 0x26B89A)
        case .cancelled: return Color(hex: 0xFF3B30)
        }
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(accentColor)
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text(visit.propertyTitle)
                        .font(.system(size: 15, weight: .bold))
                        .lineLimit(1)
                    Spacer()
                    StatusBadge(status: visit.status)
                }

                Text("\(visit.buyerName) · \(visit.buyerPhone)")
                    .font(.system(size: 13))
                    .foregroundColor(.textSecondary)

                Divider()

                HStack(spacing: 6) {
                    Image(systemName: "clock")
                        .font(.system(size: 13))
                        .foregroundColor(.textTertiary)
                    Text(visit.timeLabel)
                        .font(.system(size: 13))
                        .foregroundColor(.textSecondary)

                    Spacer()

                    if visit.status == .scheduled {
                        if isActing {
                            ProgressView()
                                .tint(.brandTeal)
                                .scaleEffect(0.9)
                        } else {
                            Button("Cancel") {
                                isActing = true
                                onCancel()
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
                                    isActing = false
                                }
                            }
                            .font(.system(size: 13))
                            .foregroundColor(Color(hex: 0xFF3B30))
                            .buttonStyle(.bordered)

                            Button {
                                isActing = true
                                onMarkComplete()
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
                                    isActing = false
                                }
                            } label: {
                                Label("Done", systemImage: "checkmark")
                                    .font(.system(size: 13))
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(Color(hex: 0x26B89A))
                        }
                    }
                }
            }
            .padding(14)
        }
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(Color.black.opacity(0.04), lineWidth: 1)
        )
        .padding(.horizontal, 14)
        .padding(.vertical, 6)
    }
}

private struct StatusBadge: View {
    let status: VisitStatus

    var body: some View {
        let style: (text: String, bg: Color, fg: Color) = {
            switch status {
            case .scheduled:
                return ("Scheduled", Color(hex: 0xDDE8FF), Color(hex: 0x3B62D9))
            case .completed:
                return ("Completed", Color(hex: 0xD6F5EC), Color(hex: 0x1A9A6C))
            case .cancelled:
                return ("Cancelled", Color(hex: 0xFFE5E5), Color(hex: 0xFF3B30))
            }
        }()

        return Text(style.text)
            .font(.system(size: 11, weight: .semibold))
            .foregroundColor(style.fg)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(style.bg)
            .clipShape(Capsule())
    }
}

private struct ScheduleVisitSheet: View {
    let propertyOptions: [String]
    let isSaving: Bool
    let errorMessage: String?
    let onSchedule: (VisitScheduleDraft) async -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var draft = VisitScheduleDraft()
    @State private var selectedProperty: String?
    @State private var formError: String?

    var body: some View {
        NavigationStack {
            Form {
                Section("Property") {
                    Picker("Select Property", selection: $selectedProperty) {
                        Text("Choose a listing").tag(Optional<String>.none)
                        ForEach(propertyOptions, id: \.self) { option in
                            Text(option).tag(Optional.some(option))
                        }
                    }
                }

                Section("Buyer") {
                    TextField("Buyer name", text: $draft.buyerName)
                    TextField("Buyer phone", text: $draft.buyerPhone)
                        .keyboardType(.phonePad)
                        .onChange(of: draft.buyerPhone) { newValue in
                            draft.buyerPhone = String(newValue.filter(\.isNumber).prefix(10))
                        }
                }

                Section("Date & Time") {
                    DatePicker("Visit Date", selection: $draft.dateTime)
                }

                if let error = formError ?? errorMessage {
                    Section {
                        Text(error)
                            .font(.system(size: 12))
                            .foregroundColor(.red)
                    }
                }
            }
            .navigationTitle("Schedule Visit")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        guard validate() else { return }
                        draft.propertyName = selectedProperty ?? ""
                        Task {
                            await onSchedule(draft)
                        }
                    } label: {
                        if isSaving {
                            ProgressView()
                        } else {
                            Text("Schedule")
                        }
                    }
                    .disabled(isSaving)
                }
            }
        }
    }

    private func validate() -> Bool {
        guard selectedProperty != nil else {
            formError = "Please select a property."
            return false
        }
        guard !draft.buyerName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            formError = "Enter buyer name."
            return false
        }
        guard !draft.buyerPhone.isEmpty else {
            formError = "Enter buyer phone."
            return false
        }
        formError = nil
        return true
    }
}

#Preview {
    VisitsView()
}
