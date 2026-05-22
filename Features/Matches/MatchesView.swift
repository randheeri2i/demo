import SwiftUI

struct MatchesView: View {
    @StateObject private var viewModel = MatchesViewModel()

    var onViewProperty: (String) -> Void = { _ in }
    var onBack: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            header
            content
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

                Text("Property Matches")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)

                Spacer()

                Button {
                    Task { await viewModel.generateNewMatches() }
                } label: {
                    HStack(spacing: 6) {
                        if viewModel.isGenerating {
                            ProgressView()
                                .tint(Color.darkNavy)
                                .scaleEffect(0.8)
                        } else {
                            Image(systemName: "arrow.clockwise")
                                .font(.system(size: 14, weight: .bold))
                        }
                        Text("Generate New Matches")
                            .font(.system(size: 12, weight: .bold))
                    }
                    .foregroundColor(Color.darkNavy)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
                .disabled(viewModel.isGenerating)
            }

            if !viewModel.isLoading && !viewModel.matches.isEmpty {
                Text("\(viewModel.matches.count) Potential Matches Found")
                    .font(.system(size: 12))
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 6)
                    .background(Color.white.opacity(0.12))
                    .clipShape(Capsule())
            } else {
                Spacer().frame(height: 8)
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.darkNavy)
    }

    @ViewBuilder
    private var content: some View {
        if let errorMessage = viewModel.errorMessage {
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: 0xE05353))
                Text(errorMessage)
                    .font(.system(size: 13))
                    .foregroundColor(Color(hex: 0xE05353))
                Spacer()
                Button {
                    viewModel.errorMessage = nil
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: 0xE05353))
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
            .background(Color(hex: 0xFFEEEE))
        }

        if viewModel.isLoading {
            Spacer()
            VStack(spacing: 12) {
                ProgressView().tint(Color.darkNavy)
                Text("Loading matches…")
                    .font(.system(size: 14))
                    .foregroundColor(.textTertiary)
            }
            Spacer()
        } else if viewModel.matches.isEmpty {
            Spacer()
            VStack(spacing: 8) {
                Image(systemName: "magnifyingglass.circle")
                    .font(.system(size: 48))
                    .foregroundColor(.textTertiary)
                Text("No matches yet")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.textPrimary)
                Text("Tap \"Generate New Matches\" to run the algorithm")
                    .font(.system(size: 13))
                    .foregroundColor(.textTertiary)
            }
            Spacer()
        } else {
            ScrollView {
                LazyVStack(spacing: 16) {
                    ForEach(viewModel.matches) { match in
                        MatchCard(match: match) {
                            onViewProperty(match.propertyID)
                        }
                    }
                }
                .padding(16)
            }
        }
    }
}

private struct MatchCard: View {
    let match: PropertyMatchItem
    let onViewProperty: () -> Void

    private var scoreColor: Color {
        if match.matchScore >= 90 { return Color(hex: 0x26B89A) }
        if match.matchScore >= 75 { return Color(hex: 0xE8943A) }
        return Color(hex: 0xE05353)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Spacer()
                Text("\(match.matchScore)%")
                    .font(.system(size: 13, weight: .heavy))
                    .foregroundColor(scoreColor)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 5)
                    .background(scoreColor.opacity(0.12))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
            }

            HStack(alignment: .center, spacing: 8) {
                MatchPanel(
                    label: "PROPERTY",
                    icon: "location.fill",
                    iconTint: .darkNavy,
                    background: Color(hex: 0xF0F4FF),
                    name: match.propertyName,
                    subline: match.propertyPrice,
                    isSublineStrong: true
                )

                Circle()
                    .fill(Color(hex: 0xE8943A).opacity(0.15))
                    .frame(width: 36, height: 36)
                    .overlay(
                        Image(systemName: "arrow.right")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color(hex: 0xE8943A))
                    )

                MatchPanel(
                    label: "BUYER DEMAND",
                    icon: "person.fill",
                    iconTint: Color(hex: 0x26B89A),
                    background: Color(hex: 0x26B89A).opacity(0.07),
                    name: match.buyerName,
                    subline: "\(match.buyerBudgetMin) -\n\(match.buyerBudgetMax)",
                    isSublineStrong: false
                )
            }

            Divider()

            HStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Match Score")
                        .font(.system(size: 11))
                        .foregroundColor(.textTertiary)
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(Color.dividerColor)
                            .frame(height: 6)
                        Capsule()
                            .fill(scoreColor)
                            .frame(width: CGFloat(match.matchScore) * 2.0, height: 6)
                    }
                    .frame(maxWidth: .infinity)
                }

                Button("View Property", action: onViewProperty)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.darkNavy)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(Color.darkNavy, lineWidth: 1.5)
                    )
            }
        }
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 6, y: 1)
    }
}

private struct MatchPanel: View {
    let label: String
    let icon: String
    let iconTint: Color
    let background: Color
    let name: String
    let subline: String
    let isSublineStrong: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label)
                .font(.system(size: 9, weight: .semibold))
                .foregroundColor(.textTertiary)
                .tracking(0.8)

            HStack(alignment: .top, spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 13))
                    .foregroundColor(iconTint)
                Text(name)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.textPrimary)
                    .lineLimit(2)
            }

            HStack(alignment: .top, spacing: 2) {
                Image(systemName: "indianrupeesign")
                    .font(.system(size: 11))
                    .foregroundColor(.textTertiary)
                Text(subline)
                    .font(.system(size: isSublineStrong ? 15 : 12, weight: isSublineStrong ? .heavy : .regular))
                    .foregroundColor(isSublineStrong ? .textPrimary : .textTertiary)
                    .lineLimit(2)
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(background)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

#Preview {
    MatchesView()
}
