package com.readrops.app.item.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.readrops.app.R
import com.readrops.app.timelime.components.itemWithFeed
import com.readrops.app.util.DefaultPreview
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ReadropsTheme
import com.readrops.db.pojo.ItemWithFeed

@Composable
fun BackgroundTitle(
    itemWithFeed: ItemWithFeed,
    onClickBack: () -> Unit,
) {
    val onScrimColor = Color.White.copy(alpha = 0.85f)
    val accentColor = if (itemWithFeed.color != 0) {
        Color(itemWithFeed.color)
    } else {
        onScrimColor
    }

    Surface(
        shape = RoundedCornerShape(
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        ),
        modifier = Modifier.height(IntrinsicSize.Max)
    ) {
        AsyncImage(
            model = itemWithFeed.item.imageLink,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_broken_image),
            modifier = Modifier
                .fillMaxSize()
        )

        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box {
                IconButton(
                    onClick = onClickBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                SimpleTitle(
                    itemWithFeed = itemWithFeed,
                    titleColor = onScrimColor,
                    accentColor = accentColor,
                    baseColor = onScrimColor,
                    bottomPadding = true
                )
            }
        }
    }

    MediumSpacer()
}

@DefaultPreview
@Composable
private fun BackgroundTitlePreview() {
    ReadropsTheme {
        BackgroundTitle(
            itemWithFeed = itemWithFeed,
            onClickBack = {}
        )
    }
}