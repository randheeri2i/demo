package com.pinmyhome.app.ui.screens.other

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pinmyhome.app.network.ApiClient
import com.pinmyhome.app.network.ApiConfig
import com.pinmyhome.app.network.ApiEndpoints
import com.pinmyhome.app.network.ApiResult
import com.pinmyhome.app.network.repositories.PropertyRepository
import com.pinmyhome.app.network.repositories.ReferenceRepository
import com.pinmyhome.app.ui.auth.getAuthToken
import com.pinmyhome.app.ui.components.AppSnackbar
import com.pinmyhome.app.ui.components.rememberSnackbarState
import com.pinmyhome.app.ui.components.showError
import com.pinmyhome.app.ui.theme.BrandTeal
import com.pinmyhome.app.ui.theme.CardWhite
import com.pinmyhome.app.ui.theme.DividerColor
import com.pinmyhome.app.ui.theme.InputBorder
import com.pinmyhome.app.ui.theme.LightBackground
import com.pinmyhome.app.ui.theme.NavyDark
import com.pinmyhome.app.ui.theme.TextPrimary
import com.pinmyhome.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

// ─── Step metadata ────────────────────────────────────────────────────────────
private data class StepMeta(val label: String, val icon: ImageVector)
private val STEPS = listOf(
    StepMeta("Society &\nLocation", Icons.Default.Domain),
    StepMeta("Seller Info", Icons.Default.Person),
    StepMeta("Property\nDetails", Icons.Default.Info),
    StepMeta("Photos", Icons.Default.CameraAlt),
    StepMeta("Review &\nSubmit", Icons.Default.CurrencyRupee)
)

// ─── Data classes ─────────────────────────────────────────────────────────────
private data class ExistingProperty(
    val tower: String,
    val flatNumber: String,
    val superArea: Int?,
    val bedrooms: Int?,
    val toilets: Int?,
    val floor: Int?,
    val sellerPrice: Long?,
    val status: String,
    val listedBy: String
)

private data class SocietyConfig(
    val id: Int,
    val propertyType: String,
    val areaSqft: Int,
    val avgPricePerSqft: Int
)

/** A photo selected from gallery (local) or pasted as a remote URL. */
private data class PropertyPhoto(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val isRemote: Boolean = uri.startsWith("http://") || uri.startsWith("https://")
)

// ─── Shared button styling ────────────────────────────────────────────────────
private object WizardButtons {
    val Height = 50.dp
    val CompactHeight = 44.dp
    val Shape = RoundedCornerShape(12.dp)
    val CompactShape = RoundedCornerShape(10.dp)
    val Border = BorderStroke(1.5.dp, InputBorder)
}

@Composable
private fun WizardPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = WizardButtons.Height,
    containerColor: Color = BrandTeal,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(height),
        shape = WizardButtons.Shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier = Modifier.width(6.dp))
            }
            Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        }
    }
}

@Composable
private fun WizardSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = WizardButtons.Height,
    contentColor: Color = TextPrimary,
    border: BorderStroke = WizardButtons.Border,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = WizardButtons.Shape,
        border = border,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, modifier = Modifier.size(16.dp), tint = contentColor)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = contentColor)
    }
}

@Composable
private fun WizardTextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = TextSecondary
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(WizardButtons.CompactHeight),
        shape = WizardButtons.CompactShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = rememberSnackbarState()

    var currentStep by remember { mutableStateOf(0) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val areas = remember { mutableStateListOf<Pair<Int, String>>() }
    val societies = remember { mutableStateListOf<Pair<Int, String>>() }
    val societyTowers = remember { mutableStateListOf<String>() }
    val configs = remember { mutableStateListOf<SocietyConfig>() }

    var showAvailabilityCheck by remember { mutableStateOf(false) }
    var isCheckingAvailability by remember { mutableStateOf(false) }
    val existingProperties = remember { mutableStateListOf<ExistingProperty>() }

    var selectedAreaId by remember { mutableStateOf<Int?>(null) }
    var selectedAreaName by remember { mutableStateOf("") }
    var selectedSocietyId by remember { mutableStateOf<Int?>(null) }
    var selectedSociety by remember { mutableStateOf("") }
    var superArea by remember { mutableStateOf("") }
    var noOfBedrooms by remember { mutableStateOf("") }
    var noOfToilets by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var priceDemanded by remember { mutableStateOf("") }

    var sellerName by remember { mutableStateOf("") }
    var sellerPhone by remember { mutableStateOf("") }

    var tower by remember { mutableStateOf("") }
    var flatNo by remember { mutableStateOf("") }
    var facing by remember { mutableStateOf("") }
    var parking by remember { mutableStateOf("") }
    var sunlight by remember { mutableStateOf("") }
    var furnishingStatus by remember { mutableStateOf("") }
    var constructionYear by remember { mutableStateOf("") }
    var vastuCompliant by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    var photoUrl by remember { mutableStateOf("") }
    val photos = remember { mutableStateListOf<PropertyPhoto>() }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 12)
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        uris.forEach { uri ->
            // Persist a read grant when the picker supports it (SAF).
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val value = uri.toString()
            if (photos.none { it.uri == value }) {
                photos.add(PropertyPhoto(uri = value, isRemote = false))
            }
        }
    }

    fun openPhotoPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun addPhotoUrl() {
        val url = photoUrl.trim()
        when {
            url.isBlank() -> showError(snackbar, "Please paste a photo URL")
            !url.startsWith("http://") && !url.startsWith("https://") ->
                showError(snackbar, "Photo URL must start with http:// or https://")
            photos.any { it.uri == url } -> showError(snackbar, "This photo is already added")
            else -> {
                photos.add(PropertyPhoto(uri = url, isRemote = true))
                photoUrl = ""
            }
        }
    }

    LaunchedEffect(Unit) {
        when (val r = ReferenceRepository.getAreas(context)) {
            is ApiResult.Success -> {
                val arr = r.data.optJSONArray("areas") ?: r.data.optJSONArray("data")
                arr?.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        areas.add(obj.optInt("id") to obj.optString("name"))
                    }
                }
            }
            is ApiResult.Error -> showError(snackbar, "Could not load areas: ${r.message}")
        }
    }

    LaunchedEffect(selectedAreaId) {
        val id = selectedAreaId ?: return@LaunchedEffect
        societies.clear()
        selectedSociety = ""; selectedSocietyId = null
        societyTowers.clear(); configs.clear()
        when (val r = ReferenceRepository.getSocieties(context, id)) {
            is ApiResult.Success -> {
                val arr = r.data.optJSONArray("societies") ?: r.data.optJSONArray("data")
                arr?.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        societies.add(obj.optInt("id") to obj.optString("name"))
                    }
                }
            }
            is ApiResult.Error -> showError(snackbar, "Could not load societies: ${r.message}")
        }
    }

    LaunchedEffect(selectedSocietyId) {
        val id = selectedSocietyId ?: return@LaunchedEffect
        societyTowers.clear(); configs.clear()
        when (val r = ReferenceRepository.getSocietyConfigurations(context, id.toString())) {
            is ApiResult.Success -> {
                val arr = r.data.optJSONArray("configurations") ?: r.data.optJSONArray("data")
                arr?.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        configs.add(
                            SocietyConfig(
                                id = obj.optInt("id"),
                                propertyType = obj.optString("property_type"),
                                areaSqft = obj.optInt("area_sqft"),
                                avgPricePerSqft = obj.optInt("avg_price_per_sqft")
                            )
                        )
                    }
                }
            }
            is ApiResult.Error -> { /* configs optional */ }
        }
    }

    LaunchedEffect(selectedSocietyId, societies.size) {
        val id = selectedSocietyId ?: return@LaunchedEffect
        societyTowers.clear()
        val aId = selectedAreaId ?: return@LaunchedEffect
        when (val r = ReferenceRepository.getSocieties(context, aId)) {
            is ApiResult.Success -> {
                val arr = r.data.optJSONArray("societies") ?: r.data.optJSONArray("data")
                arr?.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        if (obj.optInt("id") == id) {
                            val towersArr = obj.optJSONArray("towers")
                            towersArr?.let { ta ->
                                for (j in 0 until ta.length()) societyTowers.add(ta.getString(j))
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }

    fun doAvailabilityCheck() {
        val socId = selectedSocietyId ?: return
        isCheckingAvailability = true
        existingProperties.clear()
        scope.launch {
            when (val r = PropertyRepository.getAll(context, societyId = socId)) {
                is ApiResult.Success -> {
                    val arr = r.data.optJSONArray("properties") ?: r.data.optJSONArray("data")
                    arr?.let {
                        for (i in 0 until it.length()) {
                            val obj = it.getJSONObject(i)
                            existingProperties.add(
                                ExistingProperty(
                                    tower = obj.optString("tower"),
                                    flatNumber = obj.optString("flat_number"),
                                    superArea = obj.optInt("area_sqft").takeIf { v -> v > 0 },
                                    bedrooms = obj.optInt("bedrooms").takeIf { v -> v > 0 },
                                    toilets = obj.optInt("toilets").takeIf { v -> v > 0 },
                                    floor = obj.optInt("floor").takeIf { v -> v > 0 },
                                    sellerPrice = obj.optLong("price_demanded").takeIf { v -> v > 0 },
                                    status = obj.optString("status"),
                                    listedBy = obj.optString("broker_name")
                                )
                            )
                        }
                    }
                    isCheckingAvailability = false
                    showAvailabilityCheck = true
                }
                is ApiResult.Error -> {
                    isCheckingAvailability = false
                    showAvailabilityCheck = true
                }
            }
        }
    }

    fun validateStep(): String? = when (currentStep) {
        0 -> when {
            selectedAreaId == null -> "Please select an area"
            selectedSocietyId == null -> "Please select a society"
            superArea.isBlank() -> "Super area is required"
            noOfBedrooms.isBlank() -> "No. of bedrooms is required"
            noOfToilets.isBlank() -> "No. of toilets is required"
            floor.isBlank() -> "Floor number is required"
            priceDemanded.isBlank() || priceDemanded.toLongOrNull() == null ->
                "Please enter a valid price demanded"
            else -> null
        }
        2 -> when {
            tower.isBlank() -> "Tower number is required"
            flatNo.isBlank() -> "Flat number is required"
            else -> null
        }
        else -> null
    }

    fun bedroomsToBhkType(beds: String): String {
        val n = beds.toIntOrNull() ?: return beds
        return when {
            n >= 5 -> "5BHK+"
            n > 0 -> "${n}BHK"
            else -> beds
        }
    }

    fun extractPropertyId(data: JSONObject): String? {
        val direct = data.optString("id").ifBlank {
            data.optJSONObject("property")?.optString("id").orEmpty()
        }.ifBlank {
            data.optJSONObject("data")?.optString("id").orEmpty()
        }
        return direct.takeIf { it.isNotBlank() }
    }

    fun submitProperty() {
        isSubmitting = true
        scope.launch {
            val beds = noOfBedrooms.toIntOrNull() ?: 0
            val remoteUrls = photos.filter { it.isRemote }.map { it.uri }
            val localUris = photos.filter { !it.isRemote }.map { Uri.parse(it.uri) }

            val body = JSONObject().apply {
                put("society_id", selectedSocietyId)
                put("tower", tower.trim())
                put("flat_number", flatNo.trim())
                put("area_sqft", superArea.toIntOrNull() ?: 0)
                put("bhk_type", bedroomsToBhkType(noOfBedrooms))
                put("bedrooms", beds)
                put("toilets", noOfToilets.toIntOrNull() ?: 0)
                put("floor", floor.toIntOrNull() ?: 0)
                put("price_demanded", priceDemanded.toLongOrNull() ?: 0)
                put("has_vastu", vastuCompliant)
                if (sellerName.isNotBlank()) put("seller_name", sellerName.trim())
                if (sellerPhone.isNotBlank()) put("seller_phone", sellerPhone.trim())
                if (facing.isNotBlank()) put("facing", facing.lowercase())
                if (parking.isNotBlank()) put("parking", parking.lowercase())
                if (sunlight.isNotBlank()) put("sunlight", sunlight.lowercase())
                if (furnishingStatus.isNotBlank()) {
                    put("furnishing_status", furnishingStatus.lowercase().replace(" ", "-"))
                }
                constructionYear.toIntOrNull()?.let { put("construction_year", it) }
                if (description.isNotBlank()) put("description", description.trim())
            }

            when (val result = PropertyRepository.create(context, body)) {
                is ApiResult.Success -> {
                    val propertyId = extractPropertyId(result.data)
                    val hasPhotos = remoteUrls.isNotEmpty() || localUris.isNotEmpty()

                    if (!hasPhotos) {
                        isSubmitting = false
                        showSuccessDialog = true
                        return@launch
                    }

                    if (propertyId == null) {
                        isSubmitting = false
                        showError(
                            snackbar,
                            "Property created, but photo upload needs a property id from the API."
                        )
                        return@launch
                    }

                    // Remote https URLs via existing repository (JSON).
                    if (remoteUrls.isNotEmpty()) {
                        when (val upload = PropertyRepository.addPhotos(context, propertyId, remoteUrls)) {
                            is ApiResult.Success -> Unit
                            is ApiResult.Error -> {
                                isSubmitting = false
                                showError(
                                    snackbar,
                                    "Property saved, but photo URLs failed: ${upload.message}"
                                )
                                return@launch
                            }
                        }
                    }

                    // Gallery files via multipart (ApiClient is JSON-only).
                    if (localUris.isNotEmpty()) {
                        when (val upload = uploadLocalPropertyPhotos(context, propertyId, localUris)) {
                            is ApiResult.Success -> Unit
                            is ApiResult.Error -> {
                                isSubmitting = false
                                showError(
                                    snackbar,
                                    "Property saved, but gallery photos failed: ${upload.message}"
                                )
                                return@launch
                            }
                        }
                    }

                    isSubmitting = false
                    showSuccessDialog = true
                }
                is ApiResult.Error -> {
                    isSubmitting = false
                    showError(snackbar, result.message)
                }
            }
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(onDismiss = {
            showSuccessDialog = false
            onBack()
        })
    }

    if (showAvailabilityCheck) {
        AvailabilityCheckScreen(
            societyName = selectedSociety,
            existingProperties = existingProperties,
            isLoading = isCheckingAvailability,
            onContinueAnyway = {
                showAvailabilityCheck = false
                currentStep = 1
            },
            onViewExisting = { showAvailabilityCheck = false },
            onAddBuyerDemand = {
                showAvailabilityCheck = false
                onBack()
            },
            onCancel = { showAvailabilityCheck = false }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
        ) {
            TopBar(onBack = onBack)
            StepIndicator(currentStep = currentStep, steps = STEPS)
            HorizontalDivider(color = DividerColor)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState)
                            slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        else
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    },
                    label = "step_anim"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        when (step) {
                            0 -> Step0_SocietyLocation(
                                areas = areas.map { it.second },
                                selectedArea = selectedAreaName,
                                onAreaChange = { name ->
                                    selectedAreaName = name
                                    selectedAreaId = areas.firstOrNull { it.second == name }?.first
                                    selectedSociety = ""; selectedSocietyId = null
                                },
                                societies = societies.map { it.second },
                                selectedSociety = selectedSociety,
                                onSocietyChange = { name ->
                                    selectedSociety = name
                                    selectedSocietyId =
                                        societies.firstOrNull { it.second == name }?.first
                                },
                                superArea = superArea,
                                onSuperAreaChange = { superArea = it },
                                noOfBedrooms = noOfBedrooms,
                                onBedroomsChange = { noOfBedrooms = it },
                                noOfToilets = noOfToilets,
                                onToiletsChange = { noOfToilets = it },
                                floor = floor,
                                onFloorChange = { floor = it },
                                priceDemanded = priceDemanded,
                                onPriceChange = { priceDemanded = it }
                            )
                            1 -> Step1_SellerInfo(
                                sellerName = sellerName,
                                onSellerNameChange = { sellerName = it },
                                sellerPhone = sellerPhone,
                                onSellerPhoneChange = { sellerPhone = it }
                            )
                            2 -> Step2_PropertyDetails(
                                societyTowers = societyTowers,
                                tower = tower,
                                onTowerChange = { tower = it },
                                flatNo = flatNo,
                                onFlatNoChange = { flatNo = it },
                                facing = facing,
                                onFacingChange = { facing = it },
                                parking = parking,
                                onParkingChange = { parking = it },
                                sunlight = sunlight,
                                onSunlightChange = { sunlight = it },
                                furnishingStatus = furnishingStatus,
                                onFurnishingChange = { furnishingStatus = it },
                                constructionYear = constructionYear,
                                onYearChange = { constructionYear = it },
                                vastuCompliant = vastuCompliant,
                                onVastuChange = { vastuCompliant = it },
                                description = description,
                                onDescriptionChange = { description = it }
                            )
                            3 -> Step3_Photos(
                                photos = photos,
                                photoUrl = photoUrl,
                                onPhotoUrlChange = { photoUrl = it },
                                onBrowseClick = { openPhotoPicker() },
                                onAddUrl = { addPhotoUrl() },
                                onRemove = { photo -> photos.removeAll { it.id == photo.id } },
                                onMakePrimary = { photo ->
                                    val idx = photos.indexOfFirst { it.id == photo.id }
                                    if (idx > 0) {
                                        val item = photos.removeAt(idx)
                                        photos.add(0, item)
                                    }
                                }
                            )
                            4 -> Step4_ReviewSubmit(
                                society = selectedSociety,
                                tower = tower,
                                flatNo = flatNo,
                                superArea = superArea,
                                bedrooms = noOfBedrooms,
                                toilets = noOfToilets,
                                floor = floor,
                                priceDemanded = priceDemanded,
                                sellerName = sellerName,
                                photoCount = photos.size
                            )
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            BottomNavBar(
                currentStep = currentStep,
                totalSteps = STEPS.size,
                isSubmitting = isSubmitting,
                onBack = { if (currentStep == 0) onBack() else currentStep-- },
                onNext = {
                    val error = validateStep()
                    if (error != null) {
                        showError(snackbar, error)
                        return@BottomNavBar
                    }
                    when {
                        currentStep == 0 -> doAvailabilityCheck()
                        currentStep < STEPS.size - 1 -> currentStep++
                        else -> submitProperty()
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            AppSnackbar(state = snackbar)
        }
    }
}

// ─── Availability Check Screen ────────────────────────────────────────────────
@Composable
private fun AvailabilityCheckScreen(
    societyName: String,
    existingProperties: List<ExistingProperty>,
    isLoading: Boolean,
    onContinueAnyway: () -> Unit,
    onViewExisting: () -> Unit,
    onAddBuyerDemand: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Add New Property",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "List a resale flat in your portfolio",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.weight(1f))
            Text("Step 1 of 5", color = Color.White, fontSize = 13.sp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Unit Availability Check",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Existing listings in $societyName",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0F4F8),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Please check if this unit is already available on PinMyHome before continuing.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandTeal)
                        }
                    } else if (existingProperties.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF7F9FC),
                            border = BorderStroke(1.dp, Color(0xFFE5EAF0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(BrandTeal.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        tint = BrandTeal,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No properties listed yet in $societyName.",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "You're good to proceed with listing this unit.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        ExistingPropertiesTable(existingProperties)
                    }

                    Spacer(Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        WizardPrimaryButton(
                            text = "Continue Anyway →",
                            onClick = onContinueAnyway,
                            modifier = Modifier.fillMaxWidth(),
                            height = WizardButtons.CompactHeight
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (existingProperties.isNotEmpty()) {
                                WizardSecondaryButton(
                                    text = "View Existing",
                                    onClick = onViewExisting,
                                    modifier = Modifier.weight(1f),
                                    height = WizardButtons.CompactHeight,
                                    leadingIcon = Icons.Default.Visibility
                                )
                            }
                            WizardSecondaryButton(
                                text = "Buyer Demand",
                                onClick = onAddBuyerDemand,
                                modifier = Modifier.weight(1f),
                                height = WizardButtons.CompactHeight,
                                contentColor = BrandTeal,
                                border = BorderStroke(1.5.dp, BrandTeal.copy(alpha = 0.5f)),
                                leadingIcon = Icons.Default.GroupAdd
                            )
                        }
                        WizardTextActionButton(
                            text = "Cancel",
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExistingPropertiesTable(existingProperties: List<ExistingProperty>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE5EAF0)),
        color = CardWhite,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier
                    .background(Color(0xFFF7F9FC))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                listOf(
                    "Tower", "Flat", "Super Area", "Bedrooms", "Toilets",
                    "Floor", "Seller Price", "Status", "Listed By"
                ).forEach { h ->
                    Text(
                        h,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(88.dp)
                    )
                }
            }
            HorizontalDivider(color = DividerColor)
            existingProperties.forEach { prop ->
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(prop.tower, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(88.dp))
                    Text(prop.flatNumber, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(88.dp))
                    Text(
                        prop.superArea?.let { "$it sq.ft." } ?: "—",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.width(88.dp)
                    )
                    Text(
                        prop.bedrooms?.toString() ?: "—",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.width(88.dp)
                    )
                    Text(
                        prop.toilets?.toString() ?: "—",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.width(88.dp)
                    )
                    Text(
                        prop.floor?.toString() ?: "—",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.width(88.dp)
                    )
                    val priceStr = prop.sellerPrice?.let { p ->
                        when {
                            p >= 10_000_000L -> "₹%.2f Cr".format(p / 10_000_000.0)
                            p >= 100_000L -> "₹%.2f L".format(p / 100_000.0)
                            else -> "₹$p"
                        }
                    } ?: "—"
                    Text(
                        priceStr,
                        color = BrandTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(88.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (prop.status) {
                            "listed" -> Color(0xFF22C55E).copy(alpha = 0.15f)
                            else -> Color(0xFFFF9800).copy(alpha = 0.15f)
                        },
                        modifier = Modifier.width(88.dp)
                    ) {
                        Text(
                            prop.status.replaceFirstChar { it.uppercase() },
                            color = when (prop.status) {
                                "listed" -> Color(0xFF16A34A)
                                else -> Color(0xFFF57C00)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        prop.listedBy,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.width(88.dp)
                    )
                }
                HorizontalDivider(color = DividerColor)
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────
@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "Add New Property",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "List a resale flat in your portfolio",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

// ─── Step Indicator ───────────────────────────────────────────────────────────
@Composable
private fun StepIndicator(currentStep: Int, steps: List<StepMeta>) {
    val stepLabel = currentStep + 1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyDark)
            .padding(vertical = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(end = 20.dp)) {
            Text(
                "Step $stepLabel of ${steps.size}",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                val isDone = index < currentStep
                val isCurrent = index == currentStep
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> NavyDark
                                isCurrent -> BrandTeal
                                else -> Color(0xFF3A4F6E)
                            }
                        )
                        .then(if (isDone) Modifier.border(2.dp, Color.White, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isDone) Icons.Default.Check else step.icon,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (index < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (index < currentStep) Color.White else Color(0xFF3A4F6E))
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            steps.forEachIndexed { index, step ->
                Text(
                    text = step.label,
                    modifier = Modifier.weight(1f),
                    color = if (index == currentStep) BrandTeal
                    else Color.White.copy(alpha = if (index < currentStep) 0.9f else 0.5f),
                    fontSize = 9.sp,
                    fontWeight = if (index == currentStep) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

// ─── Bottom Nav Bar ───────────────────────────────────────────────────────────
@Composable
private fun BottomNavBar(
    currentStep: Int,
    totalSteps: Int,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val isLastStep = currentStep == totalSteps - 1
    Surface(shadowElevation = 8.dp, color = CardWhite, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WizardSecondaryButton(
                text = if (currentStep == 0) "Cancel" else "← Back",
                onClick = onBack,
                modifier = Modifier.weight(1f),
                leadingIcon = Icons.Default.ArrowBack
            )
            WizardPrimaryButton(
                text = if (isLastStep) "List Property" else "Continue →",
                onClick = onNext,
                modifier = Modifier.weight(2f),
                loading = isSubmitting,
                containerColor = if (isLastStep) BrandTeal else NavyDark
            )
        }
    }
}

// ─── Step 0: Society & Location ───────────────────────────────────────────────
@Composable
private fun Step0_SocietyLocation(
    areas: List<String>,
    selectedArea: String,
    onAreaChange: (String) -> Unit,
    societies: List<String>,
    selectedSociety: String,
    onSocietyChange: (String) -> Unit,
    superArea: String,
    onSuperAreaChange: (String) -> Unit,
    noOfBedrooms: String,
    onBedroomsChange: (String) -> Unit,
    noOfToilets: String,
    onToiletsChange: (String) -> Unit,
    floor: String,
    onFloorChange: (String) -> Unit,
    priceDemanded: String,
    onPriceChange: (String) -> Unit
) {
    val priceFormatted = priceDemanded.toLongOrNull()?.let { p ->
        when {
            p >= 10_000_000L -> "₹%.2f Cr".format(p / 10_000_000.0)
            p >= 100_000L -> "₹%.2f Lakh".format(p / 100_000.0)
            else -> ""
        }
    } ?: ""

    StepCard(title = "Society & Location", subtitle = "Select society and unit details") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.weight(1f)) {
                WizardDropdown(
                    value = selectedArea,
                    onValueChange = onAreaChange,
                    label = "Area",
                    placeholder = "Select area",
                    options = areas,
                    required = true
                )
            }
            Box(Modifier.weight(1f)) {
                WizardDropdown(
                    value = selectedSociety,
                    onValueChange = onSocietyChange,
                    label = "Society",
                    placeholder = if (selectedArea.isEmpty()) "Select area first" else "Select society",
                    options = societies,
                    required = true,
                    enabled = selectedArea.isNotEmpty()
                )
            }
        }

        if (selectedSociety.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionDivider("UNIT DETAILS")
            Spacer(Modifier.height(16.dp))

            Column {
                RequiredLabel("Super Area")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = superArea,
                    onValueChange = onSuperAreaChange,
                    placeholder = { Text("e.g. 1150", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Text(
                            "Sq. Ft.",
                            color = BrandTeal,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = fieldColors()
                )
            }
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    WizardDropdown(
                        value = noOfBedrooms,
                        onValueChange = onBedroomsChange,
                        label = "No. of Bedrooms",
                        placeholder = "Select",
                        options = listOf("1", "2", "3", "4", "5+"),
                        required = true
                    )
                }
                Box(Modifier.weight(1f)) {
                    WizardDropdown(
                        value = noOfToilets,
                        onValueChange = onToiletsChange,
                        label = "No. of Toilets",
                        placeholder = "Select",
                        options = listOf("1", "2", "3", "4"),
                        required = true
                    )
                }
                Box(Modifier.weight(1f)) {
                    WizardTextField(
                        value = floor,
                        onValueChange = onFloorChange,
                        label = "Floor",
                        placeholder = "e.g. 12",
                        keyboardType = KeyboardType.Number,
                        required = true
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            Column {
                RequiredLabel("Price Demanded by Seller (₹)")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = priceDemanded,
                    onValueChange = onPriceChange,
                    placeholder = { Text("e.g. 7500000", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            Icons.Default.CurrencyRupee,
                            null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = fieldColors()
                )
                if (priceFormatted.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(priceFormatted, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── Step 1: Seller Info ──────────────────────────────────────────────────────
@Composable
private fun Step1_SellerInfo(
    sellerName: String,
    onSellerNameChange: (String) -> Unit,
    sellerPhone: String,
    onSellerPhoneChange: (String) -> Unit
) {
    StepCard(title = "Seller Info", subtitle = "Details about the flat owner") {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF0F4F8),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Seller contact details help us coordinate visits and negotiations. These are kept internal and not shown to buyers.",
                modifier = Modifier.padding(14.dp),
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        WizardTextField(
            value = sellerName,
            onValueChange = onSellerNameChange,
            label = "Seller Name",
            placeholder = "e.g. Ramesh Gupta"
        )
        Spacer(Modifier.height(14.dp))
        WizardTextField(
            value = sellerPhone,
            onValueChange = onSellerPhoneChange,
            label = "Seller Phone",
            placeholder = "e.g. 9876543210",
            keyboardType = KeyboardType.Phone
        )
    }
}

// ─── Step 2: Property Details ─────────────────────────────────────────────────
@Composable
private fun Step2_PropertyDetails(
    societyTowers: List<String>,
    tower: String,
    onTowerChange: (String) -> Unit,
    flatNo: String,
    onFlatNoChange: (String) -> Unit,
    facing: String,
    onFacingChange: (String) -> Unit,
    parking: String,
    onParkingChange: (String) -> Unit,
    sunlight: String,
    onSunlightChange: (String) -> Unit,
    furnishingStatus: String,
    onFurnishingChange: (String) -> Unit,
    constructionYear: String,
    onYearChange: (String) -> Unit,
    vastuCompliant: Boolean,
    onVastuChange: (Boolean) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    StepCard(title = "Property Details", subtitle = "Features, furnishing, and description") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.weight(1f)) {
                if (societyTowers.isNotEmpty()) {
                    WizardDropdown(
                        value = tower,
                        onValueChange = onTowerChange,
                        label = "Tower Number",
                        placeholder = "Select tower",
                        options = societyTowers,
                        required = true
                    )
                } else {
                    WizardTextField(
                        value = tower,
                        onValueChange = onTowerChange,
                        label = "Tower Number",
                        placeholder = "e.g. T1",
                        required = true
                    )
                }
            }
            Box(Modifier.weight(1f)) {
                WizardTextField(
                    value = flatNo,
                    onValueChange = onFlatNoChange,
                    label = "Flat Number",
                    placeholder = "e.g. 1203",
                    keyboardType = KeyboardType.Number,
                    required = true
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionDivider("PROPERTY FEATURES")
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.weight(1f)) {
                WizardDropdown(
                    value = facing,
                    onValueChange = onFacingChange,
                    label = "Facing",
                    placeholder = "Select",
                    options = listOf("east", "west", "north", "south", "NE", "NW", "SE", "SW")
                )
            }
            Box(Modifier.weight(1f)) {
                WizardDropdown(
                    value = parking,
                    onValueChange = onParkingChange,
                    label = "Parking",
                    placeholder = "Select",
                    options = listOf("None", "Covered", "Open", "Two-Wheeler")
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.weight(1f)) {
                WizardDropdown(
                    value = sunlight,
                    onValueChange = onSunlightChange,
                    label = "Sunlight",
                    placeholder = "Select",
                    options = listOf("High", "Moderate", "Low")
                )
            }
            Box(Modifier.weight(1f)) {
                WizardDropdown(
                    value = furnishingStatus,
                    onValueChange = onFurnishingChange,
                    label = "Furnishing Status",
                    placeholder = "Select",
                    options = listOf("Unfurnished", "Semi-Furnished", "Furnished")
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        WizardTextField(
            value = constructionYear,
            onValueChange = onYearChange,
            label = "Construction Year",
            placeholder = "e.g. 2021",
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFE5EAF0)),
            color = CardWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVastuChange(!vastuCompliant) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = vastuCompliant,
                    onCheckedChange = onVastuChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = BrandTeal,
                        uncheckedColor = TextSecondary
                    )
                )
                Spacer(modifier.width(8.dp))
                Text(
                    "Vastu Compliant",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        Text("Description", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = {
                Text(
                    "Describe the property, key selling points, nearby landmarks...",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            modifier = Modifier.fillMaxWidth().height(130.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 6,
            colors = fieldColors()
        )
    }
}

// ─── Step 3: Photos ───────────────────────────────────────────────────────────
@Composable
private fun Step3_Photos(
    photos: List<PropertyPhoto>,
    photoUrl: String,
    onPhotoUrlChange: (String) -> Unit,
    onBrowseClick: () -> Unit,
    onAddUrl: () -> Unit,
    onRemove: (PropertyPhoto) -> Unit,
    onMakePrimary: (PropertyPhoto) -> Unit
) {
    StepCard(title = "Photos", subtitle = "Upload flat photos (optional)") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF5F8FB))
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFCDD5E0),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onBrowseClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CloudUpload,
                    null,
                    tint = BrandTeal,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap to choose photos from gallery",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "JPEG, PNG, WebP · up to 12 photos",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        WizardSecondaryButton(
            text = "Browse Gallery",
            onClick = onBrowseClick,
            modifier = Modifier.fillMaxWidth(),
            height = WizardButtons.CompactHeight,
            contentColor = NavyDark,
            border = BorderStroke(1.5.dp, NavyDark),
            leadingIcon = Icons.Default.AddPhotoAlternate
        )

        Spacer(Modifier.height(14.dp))
        Text("Or paste a photo URL", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = photoUrl,
                onValueChange = onPhotoUrlChange,
                placeholder = {
                    Text("https://…", color = TextSecondary, fontSize = 13.sp)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = fieldColors()
            )
            WizardSecondaryButton(
                text = "Add URL",
                onClick = onAddUrl,
                height = WizardButtons.CompactHeight,
                contentColor = NavyDark,
                border = BorderStroke(1.5.dp, NavyDark)
            )
        }

        if (photos.isNotEmpty()) {
            Spacer(modifier.height(14.dp))
            Text(
                "${photos.size} photo${if (photos.size == 1) "" else "s"} selected",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                photos.forEachIndexed { index, photo ->
                    PhotoThumbnail(
                        photo = photo,
                        isPrimary = index == 0,
                        onRemove = { onRemove(photo) },
                        onMakePrimary = { onMakePrimary(photo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: PropertyPhoto,
    isPrimary: Boolean,
    onRemove: () -> Unit,
    onMakePrimary: () -> Unit
) {
    val context = LocalContext.current
    var imageBitmap by remember(photo.uri) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }

    LaunchedEffect(photo.uri) {
        imageBitmap = withContext(Dispatchers.IO) {
            runCatching { decodePhotoBitmap(context, photo.uri) }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 90.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8EFF5))
            .clickable(enabled = !isPrimary, onClick = onMakePrimary)
    ) {
        val bmp = imageBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Image,
                    null,
                    tint = TextSecondary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        if (isPrimary) {
            Surface(
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                color = BrandTeal,
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    "Primary",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    "Set primary",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(13.dp))
        }
    }
}

private fun decodePhotoBitmap(
    context: android.content.Context,
    uriString: String
): androidx.compose.ui.graphics.ImageBitmap? {
    val options = BitmapFactory.Options().apply { inSampleSize = 4 }
    val bitmap = when {
        uriString.startsWith("http://") || uriString.startsWith("https://") -> {
            URL(uriString).openStream().use { BitmapFactory.decodeStream(it, null, options) }
        }
        else -> {
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        }
    } ?: return null
    return bitmap.asImageBitmap()
}

// ─── Step 4: Review & Submit ──────────────────────────────────────────────────
@Composable
private fun Step4_ReviewSubmit(
    society: String,
    tower: String,
    flatNo: String,
    superArea: String,
    bedrooms: String,
    toilets: String,
    floor: String,
    priceDemanded: String,
    sellerName: String,
    photoCount: Int
) {
    val priceFormatted = priceDemanded.toLongOrNull()?.let { p ->
        when {
            p >= 10_000_000L -> "₹%.2f Cr".format(p / 10_000_000.0)
            p >= 100_000L -> "₹%.2f Lakh".format(p / 100_000.0)
            else -> "₹$p"
        }
    } ?: priceDemanded

    StepCard(title = "Review & Submit", subtitle = "Confirm details and submit") {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF0FBF7),
            border = BorderStroke(1.dp, BrandTeal.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = BrandTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Commission: 10% (default)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Commission is set at 10% by default. Your admin can adjust it after reviewing the listing.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF7F9FC),
            border = BorderStroke(1.dp, Color(0xFFE5EAF0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Submission Summary",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = Color(0xFFE5EAF0))
                SummaryRow("Society", society.ifEmpty { "—" })
                SummaryRow(
                    "Tower / Flat",
                    if (tower.isNotEmpty() && flatNo.isNotEmpty()) "$tower / $flatNo" else "—"
                )
                SummaryRow("Super Area", if (superArea.isNotEmpty()) "$superArea sq.ft." else "—")
                SummaryRow(
                    "Bedrooms",
                    if (bedrooms.isNotEmpty()) {
                        "${bedrooms}BHK${if (bedrooms == "5+") "+" else ""}"
                    } else "—"
                )
                SummaryRow("Toilets", toilets.ifEmpty { "—" })
                SummaryRow("Floor", floor.ifEmpty { "—" })
                SummaryRow("Price Demanded", priceFormatted.ifEmpty { "—" })
                SummaryRow("Seller", sellerName.ifEmpty { "—" })
                SummaryRow("Photos", if (photoCount > 0) "$photoCount selected" else "None")
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ─── Success Dialog ───────────────────────────────────────────────────────────
@Composable
private fun SuccessDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = CardWhite, shadowElevation = 16.dp) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BrandTeal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = BrandTeal,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "Property Added!",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier.height(8.dp))
                Text(
                    "Your property is now under inspection.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(28.dp))
                WizardPrimaryButton(
                    text = "Done",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Shared widgets ───────────────────────────────────────────────────────────
@Composable
private fun StepCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun SectionDivider(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5EAF0))
        Text(
            "  $label  ",
            color = BrandTeal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5EAF0))
    }
}

@Composable
private fun RequiredLabel(label: String) {
    Row {
        Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(2.dp))
        Text("*", color = Color.Red, fontSize = 13.sp)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = InputBorder,
    focusedBorderColor = NavyDark,
    disabledBorderColor = DividerColor,
    unfocusedContainerColor = CardWhite,
    focusedContainerColor = CardWhite,
    disabledContainerColor = Color(0xFFF7F9FC),
    unfocusedTextColor = TextPrimary,
    focusedTextColor = TextPrimary,
    disabledTextColor = TextSecondary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    required: Boolean = false,
    enabled: Boolean = true
) {
    Column {
        Row {
            Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (required) {
                Spacer(Modifier.width(2.dp))
                Text("*", color = Color.Red, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = fieldColors()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    options: List<String>,
    required: Boolean = false,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row {
            Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (required) {
                Spacer(Modifier.width(2.dp))
                Text("*", color = Color.Red, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(placeholder, color = TextSecondary, fontSize = 13.sp) },
                trailingIcon = {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = if (enabled) TextSecondary else Color(0xFFBBC5D0)
                    )
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(10.dp),
                enabled = enabled,
                colors = fieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 13.sp, color = TextPrimary) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FormSectionLabel(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BrandTeal)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Uploads gallery-picked images as multipart/form-data.
 * Kept local to this screen so existing JSON-only [ApiClient] / [PropertyRepository.addPhotos]
 * (URL list) remain unchanged. Remote https URLs are sent via create / addPhotos instead.
 */
private suspend fun uploadLocalPropertyPhotos(
    context: Context,
    propertyId: String,
    uris: List<Uri>
): ApiResult<JSONObject> = withContext(Dispatchers.IO) {
    if (uris.isEmpty()) return@withContext ApiResult.Success(JSONObject())

    var conn: HttpURLConnection? = null
    try {
        val boundary = "----PinMyHomeBoundary${UUID.randomUUID()}"
        val token = getAuthToken(context)
        val endpoint = ApiEndpoints.Properties.addPhotos(propertyId)
        conn = (URL("${ApiConfig.BASE_URL}$endpoint").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = ApiConfig.TIMEOUT_MS
            readTimeout = ApiConfig.TIMEOUT_MS
        }

        DataOutputStream(conn.outputStream).use { out ->
            uris.forEachIndexed { index, uri ->
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val ext = when {
                    mime.contains("png") -> "png"
                    mime.contains("webp") -> "webp"
                    else -> "jpg"
                }
                val fileName = uri.lastPathSegment
                    ?.substringAfterLast('/')
                    ?.takeIf { it.contains('.') }
                    ?: "photo_${index + 1}.$ext"

                out.writeBytes("--$boundary\r\n")
                out.writeBytes(
                    "Content-Disposition: form-data; name=\"photos\"; filename=\"$fileName\"\r\n"
                )
                out.writeBytes("Content-Type: $mime\r\n\r\n")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(out)
                } ?: return@withContext ApiResult.Error("Could not read selected photo")
                out.writeBytes("\r\n")
            }
            out.writeBytes("--$boundary--\r\n")
            out.flush()
        }

        val code = conn.responseCode
        val text = if (code in 200..299) {
            conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
        } else {
            conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: "{}"
        }
        if (code in 200..299) {
            ApiResult.Success(ApiClient.normalise(text))
        } else {
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            val msg = json.optString("error").ifBlank { json.optString("message") }
                .ifBlank { "Photo upload failed (HTTP $code)" }
            ApiResult.Error(msg, code)
        }
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error("Connection timed out while uploading photos.")
    } catch (e: java.net.UnknownHostException) {
        ApiResult.Error("No internet connection.")
    } catch (e: Exception) {
        ApiResult.Error("Photo upload failed: ${e.message ?: e.javaClass.simpleName}")
    } finally {
        conn?.disconnect()
    }
}
