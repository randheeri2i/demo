import SwiftUI

struct MyBuyersView: View {
    @StateObject private var viewModel = MyBuyersViewModel()

    var body: some View {
        VStack(spacing: 0) {
            header

            HStack(spacing: 10) {
                BuyerStatPill(icon: "person.2.fill", tint: Color(hex: 0x4A6FA5), text: "\(viewModel.totalCount) Total")
                BuyerStatPill(icon: "circle.fill", tint: Color(hex: 0x27AE60), text: "\(viewModel.activeCount) Active")
                BuyerStatPill(icon: "checkmark.square.fill", tint: Color(hex: 0x4A6FA5), text: "\(viewModel.closedCount) Closed")
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)

            content
        }
        .background(Color.pageBackground)
        .task {
            await viewModel.load()
        }
    }

    private var header: some View {
        VStack(spacing: 14) {
            HStack {
                Text("My Buyers")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
            }

            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.white.opacity(0.45))
                TextField("", text: $viewModel.searchQuery, prompt: Text("Search buyers...").foregroundColor(.white.opacity(0.45)))
                    .foregroundColor(.white)
                    .font(.system(size: 14))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(Color.navyMedium)
            .clipShape(RoundedRectangle(cornerRadius: 10))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 16)
        .background(Color.darkNavy)
    }

    @ViewBuilder
    private var content: some View {
        if viewModel.isLoading {
            Spacer()
            ProgressView().tint(.brandTeal)
            Spacer()
        } else if let error = viewModel.errorMessage {
            Spacer()
            VStack(spacing: 12) {
                Image(systemName: "exclamationmark.circle")
                    .font(.system(size: 42))
                    .foregroundColor(.textTertiary)
                Text(error)
                    .font(.system(size: 14))
                    .foregroundColor(.textTertiary)
            }
            Spacer()
        } else if viewModel.filtered.isEmpty {
            Spacer()
            VStack(spacing: 12) {
                Image(systemName: "person.crop.circle.badge.exclamationmark")
                    .font(.system(size: 48))
                    .foregroundColor(.textTertiary)
                Text(viewModel.searchQuery.isEmpty ? "No buyers found" : "No results for \"\(viewModel.searchQuery)\"")
                    .font(.system(size: 14))
                    .foregroundColor(.textTertiary)
            }
            Spacer()
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(viewModel.filtered) { buyer in
                        BuyerCard(buyer: buyer)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
        }
    }
}

private struct BuyerStatPill: View {
    let icon: String
    let tint: Color
    let text: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(tint)
            Text(text)
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(Color(hex: 0x1A1D23))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .shadow(color: .black.opacity(0.05), radius: 5, y: 1)
    }
}

private struct BuyerCard: View {
    let buyer: BuyerItem

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(buyer.name)
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(Color(hex: 0x1A1D23))

            HStack(spacing: 8) {
                OutlinedBadge(label: buyer.isActive ? "Active" : "Closed", color: buyer.isActive ? Color(hex: 0x1DAA78) : .gray)

                if !buyer.flatType.isEmpty, buyer.flatType != "–" {
                    Text(buyer.flatType)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.white)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 4)
                        .background(Color(hex: 0x1DAA78))
                        .clipShape(Capsule())
                }
            }
            .padding(.top, 8)

            Divider().padding(.vertical, 14)

            ContactRow(icon: "phone.fill", value: buyer.phone)

            if !buyer.email.isEmpty {
                ContactRow(icon: "envelope.fill", value: buyer.email)
                    .padding(.top, 8)
            }

            HStack(spacing: 8) {
                Image(systemName: "indianrupeesign.circle.fill")
                    .font(.system(size: 16))
                    .foregroundColor(.gray)
                Text("\(buyer.minBudget) Cr – \(buyer.maxBudget) Cr")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
            }
            .padding(.top, 8)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 18)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 6, y: 1)
    }
}

private struct OutlinedBadge: View {
    let label: String
    let color: Color

    var body: some View {
        Text(label)
            .font(.system(size: 13, weight: .medium))
            .foregroundColor(color)
            .padding(.horizontal, 12)
            .padding(.vertical, 4)
            .overlay(
                Capsule().stroke(color, lineWidth: 1.5)
            )
    }
}

private struct ContactRow: View {
    let icon: String
    let value: String

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 15))
                .foregroundColor(.gray)
            Text(value)
                .font(.system(size: 14))
                .foregroundColor(.gray)
        }
    }
}

#Preview {
    MyBuyersView()
}
