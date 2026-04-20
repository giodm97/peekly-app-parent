package com.peekly.parent.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.peekly.parent.ui.theme.GreenDark
import com.peekly.parent.ui.theme.GreenLight
import com.peekly.parent.ui.theme.GreenMid
import com.peekly.parent.ui.theme.GreenOnContainer

data class AvatarStyle(val background: Color, val foreground: Color)

val avatarStyles = listOf(
    AvatarStyle(Color(0xFFFFCDD2), Color(0xFFB71C1C)),
    AvatarStyle(Color(0xFFE1BEE7), Color(0xFF4A148C)),
    AvatarStyle(Color(0xFFBBDEFB), Color(0xFF0D47A1)),
    AvatarStyle(Color(0xFFB2EBF2), Color(0xFF006064)),
    AvatarStyle(Color(0xFFFFE0B2), Color(0xFFE65100)),
    AvatarStyle(Color(0xFFFFF9C4), Color(0xFFF57F17)),
    AvatarStyle(Color(0xFFF8BBD0), Color(0xFF880E4F)),
    AvatarStyle(Color(0xFFC5CAE9), Color(0xFF1A237E)),
)

@Composable
fun ChildAvatar(
    name: String,
    styleIndex: Int,
    photoUri: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cornerRadius = size * 0.27f
    val baseModifier = modifier
        .size(size)
        .clip(RoundedCornerShape(cornerRadius))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    if (photoUri != null) {
        AsyncImage(
            model = photoUri,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = baseModifier
        )
    } else {
        val style = avatarStyles.getOrElse(styleIndex) { avatarStyles[0] }
        val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        val fontSize = (size.value * 0.38f).sp

        Box(
            modifier = baseModifier.background(style.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = style.foreground,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(
    childName: String,
    currentIndex: Int,
    photoUri: String?,
    onPickStyle: (Int) -> Unit,
    onPickPhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Choose an avatar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = GreenOnContainer
            )

            OutlinedButton(
                onClick = { onPickPhoto(); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenDark)
            ) {
                Icon(
                    Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Choose from gallery")
            }

            HorizontalDivider()

            Text(
                "Or pick a colour",
                style = MaterialTheme.typography.labelMedium,
                color = GreenOnContainer
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(avatarStyles) { index, _ ->
                    val isSelected = photoUri == null && index == currentIndex
                    Box(contentAlignment = Alignment.Center) {
                        ChildAvatar(
                            name = childName,
                            styleIndex = index,
                            photoUri = null,
                            size = 64.dp,
                            modifier = if (isSelected)
                                Modifier.border(3.dp, GreenDark, RoundedCornerShape(18.dp))
                            else Modifier,
                            onClick = {
                                onPickStyle(index)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
