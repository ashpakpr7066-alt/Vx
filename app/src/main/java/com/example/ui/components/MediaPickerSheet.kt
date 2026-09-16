package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DemoMediaProvider
import com.example.model.MediaType
import com.example.model.TrackType
import com.example.ui.theme.*

@Composable
fun MediaPickerSheet(
    targetTrack: TrackType,
    onAddMedia: (mediaUri: String?, drawableResName: String?, name: String, type: MediaType, durationMs: Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Android PhotoPicker (Zero permission required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onAddMedia(uri.toString(), null, "Imported Clip", MediaType.IMAGE, 5000L)
            onClose()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = VNSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Add Media to ${targetTrack.displayName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNTextPrimary
                    )
                    Text(
                        text = "Select from sample library or import from device",
                        fontSize = 11.sp,
                        color = VNTextSecondary
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VNTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PhotoPicker Button
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VNCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, VNCyan)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Photo or Video from Device", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Cinematic Stock Assets",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = VNTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(DemoMediaProvider.STOCK_LIBRARY) { stock ->
                    val resId = remember(stock.drawableResName) {
                        context.resources.getIdentifier(stock.drawableResName, "drawable", context.packageName)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(VNSurfaceVariant)
                            .border(1.dp, VNBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onAddMedia(
                                    null,
                                    stock.drawableResName,
                                    stock.title,
                                    stock.mediaType,
                                    stock.durationMs
                                )
                                onClose()
                            }
                    ) {
                        if (resId != 0) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = stock.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Gradient title overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text(
                                    text = stock.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${stock.category} • ${(stock.durationMs / 1000)}s",
                                    fontSize = 9.sp,
                                    color = VNTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
