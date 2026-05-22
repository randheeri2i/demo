import SwiftUI

struct PropertyListView: View {
    @StateObject private var viewModel = PropertyViewModel()

    var onPropertyClick: (PropertyListing) -> Void = { _ in }
    var onAddPropertyClick: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            header
            bodyContent
        }
        .background(Color.pageBackground)
        .task {
            await viewModel.loadProperties()
        }
        .onChange(of: viewModel.selectedStatus) { _ in
            Task { await viewModel.loadProperties() }
        }
    }

    private var header: some View {
        VStack(spacing: 14) {
            HStack {
                Text("Properties")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                Button(action: onAddPropertyClick) {
                    HStack(spacing: 6) {
                        Image(systemName: "plus")
                            .font(.system(size: 14, weight: .semibold))
                        Text("Add")
                            .font(.system(size: 14, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 9)
                    .background(Color.brandTeal)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
            }

            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.white.opacity(0.45))
                TextField("", text: $viewModel.searchQuery, prompt: Text("Search by society, tower, flat…").foregroundColor(.white.opacity(0.45)))
                    .foregroundColor(.white)
                    .font(.system(size: 14))
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
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

    private var bodyContent: some View {
        VStack(spacing: 0) {
            filterChips

            if viewModel.isLoading {
                Spacer()
                ProgressView()
                    .tint(.brandTeal)
                Spacer()
            } else if let error = viewModel.errorMessage {
                errorState(error: error)
            } else {
                listContent
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
    }

    private var filterChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(viewModel.statusFilters, id: \.self) { status in
                    let selected = viewModel.selectedStatus == status
                    Button {
                        viewModel.selectedStatus = status
                    } label: {
                        Text(status)
                            .font(.system(size: 13, weight: selected ? .semibold : .regular))
                            .foregroundColor(selected ? .white : .textSecondary)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(selected ? Color.darkNavy : .white)
                            .overlay(
                                RoundedRectangle(cornerRadius: 20)
                                    .stroke(selected ? Color.darkNavy : Color(hex: 0xE5EAF0), lineWidth: 1.5)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 20))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 14)
            .padding(.bottom, 10)
        }
        .background(.white)
    }

    private var listContent: some View {
        VStack(alignment: .leading, spacing: 0) {
            let count = viewModel.filteredProperties.count
            Text("\(count) propert\(count == 1 ? "y" : "ies") found")
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(.textSecondary)
                .padding(.horizontal, 16)
                .padding(.vertical, 12)

            if viewModel.filteredProperties.isEmpty {
                emptyState
            } else {
                ScrollView {
                    LazyVStack(spacing: 14) {
                        ForEach(viewModel.filteredProperties) { property in
                            PropertyCard(property: property, statusColors: viewModel.statusColors(for: property.status)) {
                                onPropertyClick(property)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 20)
                }
            }
        }
    }

    private func errorState(error: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: "wifi.slash")
                .font(.system(size: 40))
                .foregroundColor(.textSecondary)
            Text(error)
                .font(.system(size: 14))
                .foregroundColor(.textSecondary)
                .multilineTextAlignment(.center)
            Button("Retry") {
                Task { await viewModel.loadProperties() }
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

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "magnifyingglass.circle")
                .font(.system(size: 46))
                .foregroundColor(.textSecondary)
            Text("No properties found.")
                .font(.system(size: 14))
                .foregroundColor(.textSecondary)
            if viewModel.selectedStatus != "All" {
                Button("Clear filter") {
                    viewModel.selectedStatus = "All"
                }
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(.brandTeal)
            }
        }
        .frame(maxWidth: .infinity, minHeight: 240)
        .padding(24)
    }
}

private struct PropertyCard: View {
    let property: PropertyListing
    let statusColors: (bg: Color, text: Color)
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            VStack(spacing: 0) {
                ZStack(alignment: .topTrailing) {
                    ZStack(alignment: .center) {
                        LinearGradient(
                            colors: [Color(hex: 0x2A6B7C), Color(hex: 0x3A9A8A), Color(hex: 0x4DB8A0)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )

                        if let imageURLString = property.primaryPhoto ?? property.images.first,
                           let imageURL = URL(string: imageURLString) {
                            AsyncImage(url: imageURL) { phase in
                                switch phase {
                                case .success(let image):
                                    image.resizable().scaledToFill()
                                default:
                                    fallbackImage
                                }
                            }
                        } else {
                            fallbackImage
                        }
                    }
                    .frame(height: 150)
                    .clipped()

                    Text(property.status)
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(statusColors.text)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 5)
                        .background(statusColors.bg)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .padding(10)

                    if property.photoCount > 0 {
                        HStack(spacing: 5) {
                            Image(systemName: "photo")
                                .font(.system(size: 12))
                            Text("\(property.photoCount) photos")
                                .font(.system(size: 11))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.black.opacity(0.55))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
                        .padding(10)
                    }
                }

                VStack(alignment: .leading, spacing: 8) {
                    HStack(alignment: .firstTextBaseline) {
                        Text(property.name)
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.textPrimary)
                            .lineLimit(1)
                        Spacer()
                        Text(property.price)
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color(hex: 0xE67E22))
                    }

                    Text(
                        [property.location, property.tower, property.flatNumber, property.bhk]
                            .filter { !$0.isEmpty }
                            .joined(separator: " · ")
                    )
                    .font(.system(size: 12))
                    .foregroundColor(.textSecondary)
                    .lineLimit(1)

                    HStack(spacing: 8) {
                        if !property.sqft.isEmpty {
                            PropertyPill(label: property.sqft)
                        }
                        if !property.parking.isEmpty {
                            PropertyPill(label: "\(property.parking) Parking")
                        }
                        if !property.facing.isEmpty {
                            PropertyPill(label: property.facing)
                        }
                    }
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 12)
            }
            .background(.white)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .shadow(color: .black.opacity(0.08), radius: 8, y: 2)
        }
        .buttonStyle(.plain)
    }

    private var fallbackImage: some View {
        Image(systemName: "building.2.fill")
            .font(.system(size: 60))
            .foregroundColor(.white.opacity(0.35))
    }
}

private struct PropertyPill: View {
    let label: String

    var body: some View {
        Text(label)
            .font(.system(size: 12, weight: .medium))
            .foregroundColor(.textSecondary)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(Color(hex: 0xF5F8FB))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(hex: 0xE5EAF0), lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

#Preview {
    PropertyListView()
}
