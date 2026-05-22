import SwiftUI
import UIKit

struct ProfileView: View {
    @StateObject private var viewModel = ProfileViewModel()
    @State private var isEditingPersonal = false
    @State private var isEditingBroker = false
    @State private var showInviteDialog = false

    @State private var draftName = ""
    @State private var draftEmail = ""
    @State private var draftPan = ""

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView()
                    .tint(Color(hex: 0x26B89A))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.pageBackground)
            } else if let loadError = viewModel.loadError {
                VStack(spacing: 12) {
                    Text(loadError)
                        .font(.system(size: 14))
                        .foregroundColor(.textPrimary)
                    Button("Retry") {
                        Task { await viewModel.load() }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 10)
                    .background(Color(hex: 0x26B89A))
                    .foregroundColor(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.pageBackground)
            } else {
                content
            }
        }
        .task {
            await viewModel.load()
            syncDrafts()
        }
        .sheet(isPresented: $showInviteDialog) {
            InviteDialog { name, phone in
                _ = await viewModel.generateInvite(name: name, phone: phone)
                showInviteDialog = false
            }
            .presentationDetents([.medium])
        }
    }

    private var content: some View {
        VStack(spacing: 0) {
            header

            ScrollView {
                VStack(spacing: 16) {
                    if let saveError = viewModel.saveError {
                        Text(saveError)
                            .font(.system(size: 13))
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(12)
                            .background(Color.red.opacity(0.12))
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                    }

                    personalCard
                    brokerCard
                    inviteCard
                }
                .padding(.horizontal, 16)
                .padding(.top, 20)
                .padding(.bottom, 28)
            }
            .background(Color.pageBackground)
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Profile Settings")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.white)

                Spacer()

                Circle()
                    .fill(Color(hex: 0x26B89A))
                    .frame(width: 40, height: 40)
                    .overlay(
                        Text(initials(viewModel.profile.fullName))
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                    )
            }

            HStack(spacing: 8) {
                Text(viewModel.profile.role)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 6)
                    .background(Color(hex: 0xFF7A2F))
                    .clipShape(Capsule())

                Text("KYC ✓ \(viewModel.profile.kycStatus)")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 6)
                    .background(Color.white.opacity(0.12))
                    .clipShape(Capsule())
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.darkNavy)
    }

    private var personalCard: some View {
        ProfileCard(
            title: "Personal Information",
            subtitle: "Your basic profile details",
            isEditing: isEditingPersonal,
            isSaving: viewModel.isSaving,
            onEditClick: {
                syncDrafts()
                isEditingPersonal = true
            },
            onSaveClick: {
                Task {
                    let saved = await viewModel.savePersonal(
                        name: draftName.trimmingCharacters(in: .whitespacesAndNewlines),
                        email: draftEmail.trimmingCharacters(in: .whitespacesAndNewlines)
                    )
                    if saved { isEditingPersonal = false }
                }
            },
            onCancelClick: {
                isEditingPersonal = false
                syncDrafts()
            }
        ) {
            if isEditingPersonal {
                EditableField(label: "Full Name", value: $draftName, icon: "person")
                EditableField(label: "Email", value: $draftEmail, icon: "envelope")
                ReadOnlyInfoRow(label: "Phone", value: viewModel.profile.phoneNumber, icon: "phone")
                ReadOnlyInfoRow(label: "Role", value: viewModel.profile.role, icon: "checkmark.shield")
            } else {
                ReadOnlyInfoRow(label: "Full Name", value: viewModel.profile.fullName, icon: "person")
                ReadOnlyInfoRow(label: "Phone", value: viewModel.profile.phoneNumber, icon: "phone")
                ReadOnlyInfoRow(label: "Email", value: viewModel.profile.email, icon: "envelope")
                ReadOnlyInfoRow(label: "Role", value: viewModel.profile.role, icon: "checkmark.shield")
            }

            Divider().padding(.vertical, 10)
            Text("Member since \(viewModel.profile.memberSince)")
                .font(.system(size: 13))
                .foregroundColor(.textTertiary)
        }
    }

    private var brokerCard: some View {
        ProfileCard(
            title: "Broker Details",
            subtitle: "Your professional information",
            isEditing: isEditingBroker,
            isSaving: viewModel.isSaving,
            onEditClick: {
                syncDrafts()
                isEditingBroker = true
            },
            onSaveClick: {
                Task {
                    let saved = await viewModel.savePan(draftPan.uppercased())
                    if saved { isEditingBroker = false }
                }
            },
            onCancelClick: {
                isEditingBroker = false
                syncDrafts()
            }
        ) {
            ReadOnlyInfoRow(label: "Broker ID", value: viewModel.profile.brokerID, icon: "building.2")
            ReadOnlyInfoRow(label: "KYC Status", value: viewModel.profile.kycStatus, icon: "checkmark.seal")
            ReadOnlyInfoRow(label: "Account Type", value: viewModel.profile.accountType, icon: "person.2")

            if isEditingBroker {
                EditableField(label: "PAN Number", value: $draftPan, icon: "person.text.rectangle")
            } else {
                ReadOnlyInfoRow(label: "PAN Number", value: viewModel.profile.panNumber, icon: "person.text.rectangle")
            }
        }
    }

    private var inviteCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Circle()
                    .fill(Color(hex: 0xE6F7F6))
                    .frame(width: 42, height: 42)
                    .overlay(
                        Image(systemName: "person.badge.plus")
                            .font(.system(size: 18))
                            .foregroundColor(Color(hex: 0x26B89A))
                    )

                VStack(alignment: .leading, spacing: 2) {
                    Text("Invite Sub-Broker")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.textPrimary)
                    Text("Grow your network")
                        .font(.system(size: 13))
                        .foregroundColor(.textTertiary)
                }

                Spacer()

                Button("Generate") {
                    showInviteDialog = true
                }
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(Color(hex: 0x4A90A4))
            }

            if let inviteURL = viewModel.inviteURL {
                Text("Your latest invite link")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(Color(hex: 0x4A90A4))

                HStack(spacing: 10) {
                    Text(inviteURL.replacingOccurrences(of: "https://", with: ""))
                        .font(.system(size: 12))
                        .lineLimit(1)
                        .foregroundColor(.textPrimary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 11)
                        .background(Color(hex: 0xF4F6FA))
                        .clipShape(RoundedRectangle(cornerRadius: 10))

                    Button {
                        UIPasteboard.general.string = inviteURL
                        viewModel.copiedInvite = true
                    } label: {
                        HStack(spacing: 5) {
                            Image(systemName: viewModel.copiedInvite ? "checkmark" : "doc.on.doc")
                                .font(.system(size: 13, weight: .semibold))
                            Text(viewModel.copiedInvite ? "Copied!" : "Copy")
                                .font(.system(size: 13, weight: .semibold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 15)
                        .padding(.vertical, 11)
                        .background(viewModel.copiedInvite ? Color(hex: 0x2ECC71) : Color(hex: 0x26B89A))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                    .buttonStyle(.plain)
                }
            }

            if let inviteError = viewModel.inviteError {
                Text(inviteError)
                    .font(.system(size: 12))
                    .foregroundColor(.red)
            }

            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(viewModel.subBrokerCount) sub-broker\(viewModel.subBrokerCount == 1 ? "" : "s") in your network")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                    Text("Tap to view their listings →")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.6))
                }
                Spacer()
                Image(systemName: "person.3.fill")
                    .font(.system(size: 26))
                    .foregroundColor(Color(hex: 0x5B9CF6))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(Color.darkNavy)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .padding(20)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.06), radius: 6, y: 1)
    }

    private func syncDrafts() {
        draftName = viewModel.profile.fullName
        draftEmail = viewModel.profile.email
        draftPan = viewModel.profile.panNumber
    }

    private func initials(_ name: String) -> String {
        let chars = name
            .split(separator: " ")
            .prefix(2)
            .compactMap { $0.first?.uppercased() }
            .joined()
        return chars.isEmpty ? "?" : chars
    }
}

private struct ProfileCard<Content: View>: View {
    let title: String
    let subtitle: String
    let isEditing: Bool
    let isSaving: Bool
    let onEditClick: () -> Void
    let onSaveClick: () -> Void
    let onCancelClick: () -> Void
    let content: Content

    init(
        title: String,
        subtitle: String,
        isEditing: Bool,
        isSaving: Bool,
        onEditClick: @escaping () -> Void,
        onSaveClick: @escaping () -> Void,
        onCancelClick: @escaping () -> Void,
        @ViewBuilder content: () -> Content
    ) {
        self.title = title
        self.subtitle = subtitle
        self.isEditing = isEditing
        self.isSaving = isSaving
        self.onEditClick = onEditClick
        self.onSaveClick = onSaveClick
        self.onCancelClick = onCancelClick
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.textPrimary)
                    Text(subtitle)
                        .font(.system(size: 13))
                        .foregroundColor(.textTertiary)
                }
                Spacer()

                if isEditing {
                    HStack(spacing: 8) {
                        Button("Cancel", action: onCancelClick)
                            .font(.system(size: 13))
                            .foregroundColor(.textTertiary)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.textTertiary, lineWidth: 1)
                            )

                        Button(action: onSaveClick) {
                            if isSaving {
                                ProgressView().tint(.white)
                            } else {
                                Label("Save", systemImage: "checkmark")
                                    .font(.system(size: 13))
                            }
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color(hex: 0x26B89A))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .disabled(isSaving)
                    }
                } else {
                    Button(action: onEditClick) {
                        Label("Edit", systemImage: "pencil")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(Color(hex: 0x4A90A4))
                            .padding(.horizontal, 14)
                            .padding(.vertical, 6)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color(hex: 0x4A90A4), lineWidth: 1.5)
                            )
                    }
                    .buttonStyle(.plain)
                }
            }

            content
        }
        .padding(20)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.06), radius: 6, y: 1)
    }
}

private struct ReadOnlyInfoRow: View {
    let label: String
    let value: String
    let icon: String

    var body: some View {
        HStack(spacing: 14) {
            Circle()
                .fill(Color(hex: 0xEEF4F7))
                .frame(width: 42, height: 42)
                .overlay(
                    Image(systemName: icon)
                        .font(.system(size: 18))
                        .foregroundColor(Color(hex: 0x4A90A4))
                )
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(Color(hex: 0x4A90A4))
                Text(value.isEmpty ? "—" : value)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.textPrimary)
            }
            Spacer()
        }
    }
}

private struct EditableField: View {
    let label: String
    @Binding var value: String
    let icon: String

    var body: some View {
        HStack(spacing: 14) {
            Circle()
                .fill(Color(hex: 0xEEF4F7))
                .frame(width: 42, height: 42)
                .overlay(
                    Image(systemName: icon)
                        .font(.system(size: 18))
                        .foregroundColor(Color(hex: 0x4A90A4))
                )
            TextField(label, text: $value)
                .textFieldStyle(.roundedBorder)
        }
    }
}

private struct InviteDialog: View {
    let onGenerate: (String, String) async -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var phone = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Invite Sub-Broker") {
                    TextField("Name (optional)", text: $name)
                    TextField("Phone (optional)", text: $phone)
                        .keyboardType(.phonePad)
                        .onChange(of: phone) { newValue in
                            phone = String(newValue.filter(\.isNumber).prefix(10))
                        }
                }
            }
            .navigationTitle("Generate Invite")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Generate") {
                        Task { await onGenerate(name, phone) }
                    }
                }
            }
        }
    }
}

#Preview {
    ProfileView()
}
