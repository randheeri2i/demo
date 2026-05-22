import SwiftUI

struct DemandsView: View {
    @StateObject private var viewModel = DemandsViewModel()
    @State private var showAddDemand = false

    var body: some View {
        VStack(spacing: 0) {
            header

            if viewModel.isLoading {
                Spacer()
                ProgressView().tint(.brandTeal)
                Spacer()
            } else if let error = viewModel.errorMessage {
                errorState(error)
            } else {
                demandList
            }
        }
        .background(Color.pageBackground)
        .task {
            await viewModel.load()
        }
        .sheet(isPresented: $showAddDemand) {
            AddDemandSheet(
                areas: viewModel.areas,
                isSaving: viewModel.isSaving,
                onSave: { draft in
                    let saved = await viewModel.createDemand(draft)
                    if saved { showAddDemand = false }
                }
            )
            .presentationDetents([.large])
            .presentationDragIndicator(.visible)
        }
    }

    private var header: some View {
        VStack(spacing: 14) {
            HStack {
                Text("Buyer Demands")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                Button {
                    showAddDemand = true
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "plus")
                            .font(.system(size: 14, weight: .semibold))
                        Text("Add")
                            .font(.system(size: 14, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 9)
                    .background(Color(hex: 0xFF8C42))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
            }

            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.white.opacity(0.45))
                TextField("", text: $viewModel.searchQuery, prompt: Text("Search by buyer name or area…").foregroundColor(.white.opacity(0.45)))
                    .foregroundColor(.white)
                    .font(.system(size: 14))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 11)
            .background(Color.navyMedium)
            .clipShape(RoundedRectangle(cornerRadius: 10))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 16)
        .background(Color.darkNavy)
    }

    private var demandList: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                HStack(spacing: 10) {
                    DemandStatCard(emoji: "📋", value: "\(viewModel.demands.count)", label: "Demands")
                    DemandStatCard(emoji: "⭐", value: "\(viewModel.exclusiveCount)", label: "Exclusive")
                    DemandStatCard(emoji: "🎯", value: "\(viewModel.matchedCount)", label: "Matched")
                }

                if let saveError = viewModel.saveErrorMessage {
                    HStack(spacing: 8) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(Color(hex: 0x721C24))
                        Text(saveError)
                            .font(.system(size: 13))
                            .foregroundColor(Color(hex: 0x721C24))
                        Spacer()
                    }
                    .padding(12)
                    .background(Color(hex: 0xF8D7DA))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                }

                if viewModel.filteredDemands.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "magnifyingglass.circle")
                            .font(.system(size: 46))
                            .foregroundColor(.textSecondary)
                        Text("No demands found.")
                            .font(.system(size: 14))
                            .foregroundColor(.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(44)
                } else {
                    ForEach(viewModel.filteredDemands) { demand in
                        DemandCard(demand: demand)
                    }
                }
            }
            .padding(16)
        }
    }

    private func errorState(_ error: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: "wifi.slash")
                .font(.system(size: 40))
                .foregroundColor(.textSecondary)
            Text(error)
                .font(.system(size: 14))
                .foregroundColor(.textSecondary)
            Button("Retry") {
                Task { await viewModel.load() }
            }
            .font(.system(size: 14, weight: .semibold))
            .foregroundColor(.white)
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(Color.brandTeal)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(32)
    }
}

private struct DemandStatCard: View {
    let emoji: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            Text(emoji).font(.system(size: 22))
            Text(value)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.textPrimary)
            Text(label)
                .font(.system(size: 11))
                .foregroundColor(.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .shadow(color: .black.opacity(0.05), radius: 6, y: 1)
    }
}

private struct DemandCard: View {
    let demand: DemandItem

    private var initials: String {
        let pieces = demand.buyerName.split(separator: " ").prefix(2)
        return pieces.map { String($0.prefix(1)).uppercased() }.joined().isEmpty ? "?" : pieces.map { String($0.prefix(1)).uppercased() }.joined()
    }

    private var avatarColor: Color {
        let first = demand.buyerName.uppercased().first ?? "A"
        switch first {
        case "A"..."F": return Color(hex: 0x2DB5A3)
        case "G"..."L": return Color(hex: 0x4A6FA5)
        case "M"..."R": return Color(hex: 0x7B5EA7)
        default: return Color(hex: 0x3D7EAA)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top) {
                Circle()
                    .fill(avatarColor)
                    .frame(width: 46, height: 46)
                    .overlay(
                        Text(initials)
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                    )

                VStack(alignment: .leading, spacing: 2) {
                    Text(demand.buyerName)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.textPrimary)
                    HStack(spacing: 4) {
                        Image(systemName: "phone.fill")
                            .font(.system(size: 12))
                            .foregroundColor(.textTertiary)
                        Text(demand.buyerPhone)
                            .font(.system(size: 13))
                            .foregroundColor(.textTertiary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                VStack(alignment: .trailing, spacing: 4) {
                    if demand.isExclusive {
                        DemandTagBadge(text: "EXCLUSIVE", bgColor: Color(hex: 0xEDE8FF), textColor: Color(hex: 0x7B5EA7))
                    }
                    if demand.matchingCount > 0 {
                        DemandTagBadge(text: "\(demand.matchingCount) Matches", bgColor: Color(hex: 0xE3F2FD), textColor: Color(hex: 0x1565C0))
                    }
                }
            }

            Divider()
                .padding(.vertical, 12)

            HStack(spacing: 8) {
                DemandDetailChip(icon: "🛏", text: demand.flatType)
                if !demand.areaName.isEmpty {
                    DemandDetailChip(icon: "📍", text: demand.areaName)
                }
                DemandDetailChip(icon: "💰", text: demand.budgetDisplay)
            }

            HStack {
                Text("Added \(demand.createdAt)")
                    .font(.system(size: 11))
                    .foregroundColor(.textTertiary)

                Spacer()

                if demand.isExclusive {
                    HStack(spacing: 4) {
                        Image(systemName: "star.fill")
                            .font(.system(size: 12))
                            .foregroundColor(.brandOrange)
                        Text("Exclusive Mandate")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(.brandOrange)
                    }
                }
            }
            .padding(.top, 10)
        }
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.06), radius: 6, y: 1)
    }
}

private struct DemandTagBadge: View {
    let text: String
    let bgColor: Color
    let textColor: Color

    var body: some View {
        Text(text)
            .font(.system(size: 11, weight: .semibold))
            .foregroundColor(textColor)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(bgColor)
            .clipShape(Capsule())
    }
}

private struct DemandDetailChip: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 4) {
            Text(icon)
                .font(.system(size: 12))
            Text(text)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(.textPrimary)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 5)
        .background(Color(hex: 0xF5F6FA))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

private struct AddDemandSheet: View {
    let areas: [AreaOption]
    let isSaving: Bool
    let onSave: (BuyerDemandDraft) async -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var draft = BuyerDemandDraft()
    @State private var formError: String?

    private let flatTypes = ["1BHK", "2BHK", "3BHK", "4BHK", "Villa", "Plot"]

    var body: some View {
        NavigationStack {
            Form {
                Section("Buyer") {
                    TextField("Buyer name", text: $draft.name)
                    TextField("Phone", text: $draft.phone)
                        .keyboardType(.numberPad)
                        .onChange(of: draft.phone) { newValue in
                            draft.phone = String(newValue.filter(\.isNumber).prefix(10))
                        }
                }

                Section("Demand") {
                    TextField("Min Budget (₹)", text: $draft.minBudget)
                        .keyboardType(.numberPad)
                    TextField("Max Budget (₹)", text: $draft.maxBudget)
                        .keyboardType(.numberPad)

                    Picker("Flat type", selection: $draft.flatType) {
                        Text("Select type").tag("")
                        ForEach(flatTypes, id: \.self) { type in
                            Text(type).tag(type)
                        }
                    }

                    Picker("Preferred area", selection: $draft.areaID) {
                        Text("Any Area").tag(Optional<Int>.none)
                        ForEach(areas) { area in
                            Text(area.name).tag(Optional.some(area.id))
                        }
                    }

                    Toggle("Exclusive Buyer", isOn: $draft.isExclusive)
                }

                if let formError {
                    Section {
                        Text(formError)
                            .font(.system(size: 12))
                            .foregroundColor(.red)
                    }
                }
            }
            .navigationTitle("Add Buyer Demand")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        guard validate() else { return }
                        Task {
                            await onSave(draft)
                        }
                    } label: {
                        if isSaving {
                            ProgressView()
                        } else {
                            Text("Save")
                        }
                    }
                    .disabled(isSaving)
                }
            }
        }
    }

    private func validate() -> Bool {
        if draft.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            formError = "Buyer name is required."
            return false
        }

        if draft.phone.count != 10 {
            formError = "Enter a valid 10-digit phone number."
            return false
        }

        if draft.flatType.isEmpty {
            formError = "Select a flat type."
            return false
        }

        guard let min = Int64(draft.minBudget), let max = Int64(draft.maxBudget) else {
            formError = "Enter valid budget values."
            return false
        }

        if min > max {
            formError = "Min budget must be less than or equal to max budget."
            return false
        }

        formError = nil
        return true
    }
}

#Preview {
    DemandsView()
}
