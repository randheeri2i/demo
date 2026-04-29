package com.pinmyhome.app.ui.screens.other

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinmyhome.app.network.ApiResult
import com.pinmyhome.app.network.repositories.BrokerRepository
import com.pinmyhome.app.network.repositories.DemandsRepository
import com.pinmyhome.app.ui.components.BuyerDemand
import com.pinmyhome.app.ui.theme.BrandTeal
import com.pinmyhome.app.ui.theme.GreenActive
import com.pinmyhome.app.ui.theme.GreyInactive
import com.pinmyhome.app.ui.theme.NavyDark
import com.pinmyhome.app.ui.theme.NavyMedium
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyBuyersScreen(
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var demands by remember { mutableStateOf<List<BuyerDemand>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadMyBuyers() {
        scope.launch {
            isLoading = true
            when (val result = BrokerRepository.getMyBuyers(context)) {
                is ApiResult.Success -> {
                    demands = parseBuyerDemands(result.data)
                    errorMessage = null
                }
                is ApiResult.Error -> {
                    errorMessage = result.message
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadMyBuyers() }

    val totalCount = demands.size
    val activeCount = demands.count { !it.tag.equals("closed", ignoreCase = true) }
    val closedCount = demands.count { it.tag.equals("closed", ignoreCase = true) }

    val filtered = demands.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.area.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Buyers",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyMedium)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search buyers...",
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 14.sp
                                )
                            }
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BuyerStatPill(
                    icon = Icons.Default.Group,
                    iconTint = Color(0xFF4A6FA5),
                    count = "$totalCount Total",
                    modifier = Modifier.weight(1f)
                )
                BuyerStatPill(
                    icon = Icons.Default.FiberManualRecord,
                    iconTint = GreenActive,
                    count = "$activeCount Active",
                    modifier = Modifier.weight(1f)
                )
                BuyerStatPill(
                    icon = Icons.Default.CheckBox,
                    iconTint = Color(0xFF4A6FA5),
                    count = "$closedCount Closed",
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandTeal)
                    }
                }
                errorMessage != null && demands.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Failed to load buyers",
                            color = Color(0xFFB00020),
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filtered) { demand ->
                            val isClosed = demand.tag.equals("closed", ignoreCase = true)
                            BuyerCard(
                                demand = demand,
                                isActive = !isClosed,
                                visitCount = 0,
                                dealCount = 0
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { if (!isSaving) showDialog = true },
            containerColor = BrandTeal,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Buyer")
        }

        if (showDialog) {
            AddBuyerDemandDialog(
                onDismiss = { if (!isSaving) showDialog = false },
                onSave = { newDemand ->
                    scope.launch {
                        isSaving = true
                        val result = DemandsRepository.create(
                            context = context,
                            buyerName = newDemand.name,
                            buyerPhone = newDemand.phone,
                            flatType = newDemand.flatType,
                            budgetMin = budgetToRupees(newDemand.minBudget),
                            budgetMax = budgetToRupees(newDemand.maxBudget),
                            isExclusive = newDemand.isExclusive
                        )
                        when (result) {
                            is ApiResult.Success -> {
                                showDialog = false
                                loadMyBuyers()
                            }
                            is ApiResult.Error -> {
                                errorMessage = result.message
                            }
                        }
                        isSaving = false
                    }
                }
            )
        }
    }
}

@Composable
private fun BuyerStatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    count: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(count, color = Color(0xFF1A1D23), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BuyerCard(
    demand: BuyerDemand,
    isActive: Boolean,
    visitCount: Int,
    dealCount: Int
) {
    val statusColor = if (isActive) GreenActive else GreyInactive
    val statusLabel = if (isActive) "ACTIVE" else "CLOSED"
    val statusBg = if (isActive) Color(0xFFEAF9F1) else Color(0xFFF0F1F4)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isActive) NavyDark else Color(0xFFCDD0D6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = demand.name.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = demand.name,
                            color = Color(0xFF1A1D23),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = GreyInactive,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(demand.phone, color = GreyInactive, fontSize = 13.sp)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(statusLabel, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BuyerChip(icon = "🛏", label = demand.flatType)
                BuyerChip(icon = "📍", label = demand.area)
                BuyerChip(icon = "💰", label = "₹${demand.minBudget}–${demand.maxBudget}")
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F1F4))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("$visitCount visits", color = GreyInactive, fontSize = 13.sp)
                    Text("$dealCount deals", color = GreyInactive, fontSize = 13.sp)
                }

                if (isActive) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Visit",
                                color = Color(0xFF1A1D23),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Match",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyerChip(icon: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F6FA))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        Text(label, color = Color(0xFF1A1D23), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

private fun parseBuyerDemands(response: JSONObject): List<BuyerDemand> {
    val rawArray = extractBuyersArray(response) ?: return emptyList()
    val mapped = mutableListOf<BuyerDemand>()
    for (i in 0 until rawArray.length()) {
        val item = rawArray.optJSONObject(i) ?: continue
        mapped += toBuyerDemand(item)
    }
    return mapped
}

private fun extractBuyersArray(response: JSONObject): JSONArray? {
    if (response.has("data") && response.opt("data") is JSONArray) return response.optJSONArray("data")
    if (response.has("buyers") && response.opt("buyers") is JSONArray) return response.optJSONArray("buyers")
    if (response.has("results") && response.opt("results") is JSONArray) return response.optJSONArray("results")

    val dataObj = response.optJSONObject("data")
    if (dataObj != null) {
        if (dataObj.has("buyers") && dataObj.opt("buyers") is JSONArray) return dataObj.optJSONArray("buyers")
        if (dataObj.has("results") && dataObj.opt("results") is JSONArray) return dataObj.optJSONArray("results")
    }

    return null
}

private fun toBuyerDemand(item: JSONObject): BuyerDemand {
    val buyerName = item.optString("buyer_name").ifBlank { item.optString("name") }.ifBlank { "Unknown" }
    val buyerPhone = item.optString("buyer_phone").ifBlank { item.optString("phone") }.ifBlank { "-" }
    val flatType = item.optString("flat_type").ifBlank { item.optString("bhk_type") }.ifBlank { "N/A" }
    val area = item.optString("area_name").ifBlank { item.optString("area") }.ifBlank { "Any Area" }

    val minBudgetRaw = item.optLong("budget_min", item.optLong("min_budget", 0))
    val maxBudgetRaw = item.optLong("budget_max", item.optLong("max_budget", 0))

    return BuyerDemand(
        name = buyerName,
        phone = buyerPhone,
        minBudget = formatRupeesAsCrore(minBudgetRaw),
        maxBudget = formatRupeesAsCrore(maxBudgetRaw),
        flatType = flatType,
        area = area,
        isExclusive = item.optBoolean("is_exclusive", false),
        addedDate = Date(),
        tag = item.optString("status").ifBlank { null }
    )
}

private fun formatRupeesAsCrore(amount: Long): String {
    if (amount <= 0L) return "0"
    val crore = amount / 10_000_000.0
    return DecimalFormat("0.#").format(crore)
}

private fun budgetToRupees(raw: String): Long {
    val cleaned = raw
        .lowercase(Locale.getDefault())
        .replace("₹", "")
        .replace(",", "")
        .replace("cr", "")
        .trim()
    val value = cleaned.toDoubleOrNull() ?: return 0L
    return (value * 10_000_000L).toLong()
}
