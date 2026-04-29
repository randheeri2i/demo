# demo

Use a load-state flag so the icon appears both when `imageUrl` is empty and when image loading fails:

```kotlin
@Composable
fun PropertyPhotoCard(imageUrl: String?) {
    var imageLoadFailed by remember(imageUrl) { mutableStateOf(false) }
    val hasImage = !imageUrl.isNullOrBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasImage && !imageLoadFailed) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Property photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { imageLoadFailed = true },
                onSuccess = { imageLoadFailed = false }
            )
        }

        if (!hasImage || imageLoadFailed) {
            Icon(
                imageVector = Icons.Default.Domain,
                contentDescription = "No property photo",
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
            )
        }
    }
}
```
