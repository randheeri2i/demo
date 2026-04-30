package com.pinmyhome.app.ui.screens.other

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinmyhome.app.network.ApiResult
import com.pinmyhome.app.network.models.User
import com.pinmyhome.app.network.repositories.AuthRepository
import com.pinmyhome.app.network.repositories.BrokerRepository
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val NavyHeader = Color(0xFF243354)
private val SurfaceGray = Color(0xFFF4F6FA)
private val PrimaryText = Color(0xFF1B2A4A)
private val LabelColor = Color(0xFF4A90A4)
private val SubtitleColor = Color(0xFF9BA8BF)
private val TealAccent = Color(0xFF26B89A)
private val BlueAccent = Color(0xFF4A90A4)
private val IconBg = Color(0xFFEEF4F7)
private val TealIconBg = Color(0xFFE6F7F6)
private val DividerColor = Color(0xFFE8ECF4)
private val OrangeTag = Color(0xFFFF7A2F)

data class ProfileData(
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val role: String = "",
    val memberSince: String = "",
    val brokerId: String = "-",
    val kycStatus: String = "Pending",
    val panNumber: String = "",
    val accountType: String = "Independent Broker"
)

private data class InviteUiData(
    val inviteLink: String = "",
    val totalInvites: Int = 0,
    val activeInvites: Int = 0,
    val latestInviteCreatedAt: String? = null
)

private data class BrokerNetworkUiData(
    val subBrokerCount: Int = 0,
    val totalSubBrokerProperties: Int = 0,
    val buyersCount: Int = 0
)

@Composable
fun ProfileScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var profile by remember { mutableStateOf(ProfileData()) }
    var inviteData by remember { mutableStateOf(InviteUiData()) }
    var networkData by remember { mutableStateOf(BrokerNetworkUiData()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshingInvite by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isEditingPersonal by remember { mutableStateOf(false) }
    var isEditingBroker by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf(profile.fullName) }
    var draftPhone by remember { mutableStateOf(profile.phoneNumber) }
    var draftEmail by remember { mutableStateOf(profile.email) }
    var draftPan by remember { mutableStateOf(profile.panNumber) }
    var draftAccount by remember { mutableStateOf(profile.accountType) }

    fun loadProfileData() {
        scope.launch {
            isLoading = true
            errorMessage = null

            val meResult = AuthRepository.getMe(context)
            if (meResult is ApiResult.Success) {
                profile = meResult.data.toProfileData()
                draftName = profile.fullName
                draftPhone = profile.phoneNumber
                draftEmail = profile.email
                draftPan = profile.panNumber
                draftAccount = profile.accountType
            } else if (meResult is ApiResult.Error) {
                errorMessage = meResult.message
            }

            when (val invitesResult = BrokerRepository.getInvites(context)) {
                is ApiResult.Success -> {
                    inviteData = parseInvites(invitesResult.data)
                }
                is ApiResult.Error -> {
                    if (errorMessage == null) errorMessage = invitesResult.message
                }
            }

            val subBrokerCount = when (val subBrokerResult = BrokerRepository.getSubBrokers(context)) {
                is ApiResult.Success -> parseSubBrokerNetwork(subBrokerResult.data)
                is ApiResult.Error -> {
                    if (errorMessage == null) errorMessage = subBrokerResult.message
                    BrokerNetworkUiData()
                }
            }

            val buyersCount = when (val buyersResult = BrokerRepository.getMyBuyers(context)) {
                is ApiResult.Success -> parseBuyersCount(buyersResult.data)
                is ApiResult.Error -> {
                    if (errorMessage == null) errorMessage = buyersResult.message
                    0
                }
            }

            networkData = subBrokerCount.copy(buyersCount = buyersCount)
            isLoading = false
        }
    }

    fun createNewInvite() {
        scope.launch {
            isRefreshingInvite = true
            when (val result = BrokerRepository.createInvite(context)) {
                is ApiResult.Success -> {
                    val linkFromCreate = parseInviteUrl(result.data)
                    val refreshedInvites = BrokerRepository.getInvites(context)
                    if (refreshedInvites is ApiResult.Success) {
                        inviteData = parseInvites(refreshedInvites.data).copy(
                            inviteLink = linkFromCreate.ifBlank { parseInvites(refreshedInvites.data).inviteLink }
                        )
                    } else {
                        inviteData = inviteData.copy(inviteLink = linkFromCreate.ifBlank { inviteData.inviteLink })
                    }
                    errorMessage = null
                }
                is ApiResult.Error -> {
                    errorMessage = result.message
                }
            }
            isRefreshingInvite = false
        }
    }

    LaunchedEffect(Unit) {
        loadProfileData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray)
    ) {
        Column(modifier = Modifier.background(NavyHeader)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Profile Settings",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TealAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.fullName.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                            .ifBlank { "?" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OrangeTag)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = profile.role.ifBlank { "Broker" },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "KYC ✓ ${profile.kycStatus}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TealAccent)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F2))
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            ProfileCard(
                title = "Personal Information",
                subtitle = "Loaded from /auth/me",
                isEditing = isEditingPersonal,
                onEditClick = {
                    draftName = profile.fullName
                    draftPhone = profile.phoneNumber
                    draftEmail = profile.email
                    isEditingPersonal = true
                },
                onSaveClick = {
                    profile = profile.copy(
                        fullName = draftName.trim(),
                        phoneNumber = draftPhone.trim(),
                        email = draftEmail.trim()
                    )
                    isEditingPersonal = false
                },
                onCancelClick = { isEditingPersonal = false }
            ) {
                if (isEditingPersonal) {
                    EditableField("Full Name", draftName, { draftName = it }, Icons.Outlined.Person, IconBg, BlueAccent)
                    EditableField("Phone Number", draftPhone, { draftPhone = it }, Icons.Outlined.Phone, IconBg, BlueAccent, KeyboardType.Phone)
                    EditableField("Email", draftEmail, { draftEmail = it }, Icons.Outlined.Email, IconBg, BlueAccent, KeyboardType.Email)
                    ReadOnlyInfoRow("Role", profile.role, Icons.Outlined.Shield, IconBg, BlueAccent)
                } else {
                    ReadOnlyInfoRow("Full Name", profile.fullName, Icons.Outlined.Person, IconBg, BlueAccent)
                    ReadOnlyInfoRow("Phone Number", profile.phoneNumber, Icons.Outlined.Phone, IconBg, BlueAccent)
                    ReadOnlyInfoRow("Email", profile.email, Icons.Outlined.Email, IconBg, BlueAccent)
                    ReadOnlyInfoRow("Role", profile.role, Icons.Outlined.Shield, IconBg, BlueAccent)
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Member since ${profile.memberSince.ifBlank { "-" }}",
                    fontSize = 13.sp,
                    color = SubtitleColor
                )
            }

            ProfileCard(
                title = "Broker Details",
                subtitle = "Loaded from /auth/me",
                isEditing = isEditingBroker,
                onEditClick = {
                    draftPan = profile.panNumber
                    draftAccount = profile.accountType
                    isEditingBroker = true
                },
                onSaveClick = {
                    profile = profile.copy(
                        panNumber = draftPan.trim(),
                        accountType = draftAccount.trim()
                    )
                    isEditingBroker = false
                },
                onCancelClick = { isEditingBroker = false }
            ) {
                ReadOnlyInfoRow("Broker ID", profile.brokerId, Icons.Outlined.Apartment, TealIconBg, TealAccent)
                ReadOnlyInfoRow("KYC Status", profile.kycStatus, Icons.Outlined.Verified, TealIconBg, TealAccent)
                if (isEditingBroker) {
                    EditableField("PAN Number", draftPan, { draftPan = it.uppercase() }, Icons.Outlined.Person, TealIconBg, TealAccent)
                    EditableField("Account Type", draftAccount, { draftAccount = it }, Icons.Outlined.ManageAccounts, TealIconBg, TealAccent)
                } else {
                    ReadOnlyInfoRow("PAN Number", profile.panNumber, Icons.Outlined.Person, TealIconBg, TealAccent)
                    ReadOnlyInfoRow("Account Type", profile.accountType, Icons.Outlined.ManageAccounts, TealIconBg, TealAccent)
                }
            }

            InviteSubBrokerCard(
                inviteData = inviteData,
                networkData = networkData,
                isRefreshingInvite = isRefreshingInvite,
                onCreateInvite = { createNewInvite() }
            )

            OutlinedButton(
                onClick = { loadProfileData() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BlueAccent),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Refresh Profile Data", color = BlueAccent, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun InviteSubBrokerCard(
    inviteData: InviteUiData,
    networkData: BrokerNetworkUiData,
    isRefreshingInvite: Boolean,
    onCreateInvite: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val inviteLink = inviteData.inviteLink.ifBlank { "No invite generated yet" }
    val bannerText = "${networkData.subBrokerCount} sub-brokers | ${networkData.totalSubBrokerProperties} properties"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(TealIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = TealAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Invite Sub-Broker",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Text(
                        text = "POST /broker/invites + GET /broker/invites",
                        fontSize = 12.sp,
                        color = SubtitleColor
                    )
                }
                Button(
                    onClick = onCreateInvite,
                    enabled = !isRefreshingInvite,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    if (isRefreshingInvite) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isRefreshingInvite) "Creating" else "New Invite",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your invite link",
                fontSize = 12.sp,
                color = LabelColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceGray)
                        .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 11.dp)
                ) {
                    Text(
                        text = inviteLink,
                        color = PrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (copied) Color(0xFF2ECC71) else TealAccent)
                        .clickable(enabled = inviteData.inviteLink.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(inviteData.inviteLink))
                            copied = true
                        }
                        .padding(horizontal = 18.dp, vertical = 11.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (copied) "Copied!" else "Copy",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileTag("Total invites: ${inviteData.totalInvites}")
                ProfileTag("Active: ${inviteData.activeInvites}")
            }
            inviteData.latestInviteCreatedAt?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last invite: $it",
                    fontSize = 12.sp,
                    color = SubtitleColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyHeader)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = bannerText,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "My buyers: ${networkData.buyersCount}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = Color(0xFF5B9CF6),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(IconBg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = PrimaryText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileCard(
    title: String,
    subtitle: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text(subtitle, fontSize = 13.sp, color = SubtitleColor)
                }

                AnimatedContent(targetState = isEditing, label = "editToggle") { editing ->
                    if (editing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onCancelClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, SubtitleColor),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Cancel", fontSize = 13.sp, color = SubtitleColor)
                            }
                            Button(
                                onClick = onSaveClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save", fontSize = 13.sp)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueAccent),
                            border = BorderStroke(1.5.dp, BlueAccent),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
private fun ReadOnlyInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, fontSize = 13.sp, color = LabelColor, fontWeight = FontWeight.Medium)
            Text(value.ifBlank { "-" }, fontSize = 15.sp, color = PrimaryText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealAccent,
                unfocusedBorderColor = DividerColor,
                focusedLabelColor = TealAccent,
                cursorColor = TealAccent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun User.toProfileData(): ProfileData {
    val brokerInfo = broker
    return ProfileData(
        fullName = name,
        phoneNumber = phone,
        email = email,
        role = role.humanizeRole().ifBlank { "Broker" },
        memberSince = formatMemberSince(createdAt),
        brokerId = brokerInfo?.id?.toString()?.let { "#$it" } ?: "#$id",
        kycStatus = brokerInfo?.kycStatus?.humanizeWords().ifBlank { "Pending" },
        panNumber = panNumber,
        accountType = brokerInfo?.accountType?.humanizeWords().ifBlank { "Independent Broker" }
    )
}

private fun parseInvites(response: JSONObject): InviteUiData {
    val invites = extractFlexibleArray(response, "invites")
        ?: extractFlexibleArray(response, "data")
    if (invites == null || invites.length() == 0) return InviteUiData()

    var inviteUrl = ""
    var activeCount = 0
    var latestCreatedAt: String? = null

    for (i in 0 until invites.length()) {
        val invite = invites.optJSONObject(i) ?: continue
        if (inviteUrl.isBlank()) {
            inviteUrl = parseInviteUrl(invite)
        }

        val status = invite.optString("status").lowercase(Locale.getDefault())
        val expiresAt = invite.optString("expires_at")
        if (status == "active" || (status.isBlank() && !isInviteExpired(expiresAt))) {
            activeCount++
        }

        val createdAt = invite.optString("created_at")
        if (latestCreatedAt == null || createdAt > latestCreatedAt.orEmpty()) {
            latestCreatedAt = createdAt
        }
    }

    return InviteUiData(
        inviteLink = inviteUrl,
        totalInvites = invites.length(),
        activeInvites = activeCount,
        latestInviteCreatedAt = latestCreatedAt?.let { formatIsoDate(it) }
    )
}

private fun parseInviteUrl(response: JSONObject): String {
    return response.optString("invite_url")
        .ifBlank { response.optString("url") }
        .ifBlank {
            response.optJSONObject("invite")?.optString("invite_url").orEmpty()
        }
        .ifBlank {
            val invites = extractFlexibleArray(response, "invites")
            invites?.optJSONObject(0)?.optString("invite_url").orEmpty()
        }
}

private fun parseSubBrokerNetwork(response: JSONObject): BrokerNetworkUiData {
    val subBrokers = extractFlexibleArray(response, "sub_brokers")
        ?: extractFlexibleArray(response, "brokers")
        ?: extractFlexibleArray(response, "data")
        ?: JSONArray()

    var totalProperties = 0
    for (i in 0 until subBrokers.length()) {
        val item = subBrokers.optJSONObject(i) ?: continue
        totalProperties += item.optInt("property_count", item.optInt("properties_count", 0))
    }

    return BrokerNetworkUiData(
        subBrokerCount = subBrokers.length(),
        totalSubBrokerProperties = totalProperties
    )
}

private fun parseBuyersCount(response: JSONObject): Int {
    val buyers = extractFlexibleArray(response, "buyers")
        ?: extractFlexibleArray(response, "data")
    return buyers?.length() ?: 0
}

private fun extractFlexibleArray(response: JSONObject, key: String): JSONArray? {
    if (response.opt(key) is JSONArray) return response.optJSONArray(key)
    val dataObj = response.optJSONObject("data")
    if (dataObj != null && dataObj.opt(key) is JSONArray) return dataObj.optJSONArray(key)
    val resultsObj = response.optJSONObject("results")
    if (resultsObj != null && resultsObj.opt(key) is JSONArray) return resultsObj.optJSONArray(key)
    if (key == "data" && response.opt("data") is JSONArray) return response.optJSONArray("data")
    return null
}

private fun isInviteExpired(expiresAtRaw: String): Boolean {
    if (expiresAtRaw.isBlank()) return false
    val expiryDate = parseIsoDate(expiresAtRaw) ?: return false
    return expiryDate.before(Date())
}

private fun formatMemberSince(createdAtRaw: String): String {
    if (createdAtRaw.isBlank()) return ""
    return formatIsoDate(createdAtRaw)
}

private fun formatIsoDate(raw: String): String {
    if (raw.isBlank()) return ""
    val date = parseIsoDate(raw) ?: return raw
    return SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)
}

private fun parseIsoDate(raw: String): Date? {
    val candidates = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd"
    )
    val trimmed = raw.trim()
    for (pattern in candidates) {
        val parser = SimpleDateFormat(pattern, Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val parseInput = if (pattern == "yyyy-MM-dd") trimmed.take(10) else trimmed
        val parsed = runCatching { parser.parse(parseInput) }.getOrNull()
        if (parsed != null) return parsed
    }
    return null
}

private fun String.humanizeRole(): String = replace("_", " ").humanizeWords()

private fun String.humanizeWords(): String {
    if (isBlank()) return ""
    return trim()
        .replace("_", " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
        part.lowercase(Locale.getDefault()).replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen()
    }
}
