package com.readrops.app.compose.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import com.readrops.app.compose.R
import com.readrops.app.compose.util.FeedColors
import com.readrops.app.compose.util.theme.spacing

data class BottomBarState(
    val isRead: Boolean = false,
    val isStarred: Boolean = false
)

@Composable
fun ItemScreenBottomBar(
    state: BottomBarState,
    accentColor: Color,
    onShare: () -> Unit,
    onOpenUrl: () -> Unit,
    onChangeReadState: (Boolean) -> Unit,
    onChangeStarState: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (FeedColors.isColorDark(accentColor.toArgb()))
        Color.White
    else
        Color.Black

    Surface(
        color = accentColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(MaterialTheme.spacing.shortSpacing)
        ) {
            IconButton(
                onClick = { onChangeReadState(!state.isRead) }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (state.isRead)
                            R.drawable.ic_remove_done
                        else R.drawable.ic_done_all
                    ),
                    tint = tint,
                    contentDescription = null
                )
            }

            IconButton(
                onClick = { onChangeStarState(!state.isStarred) }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (state.isStarred)
                            R.drawable.ic_star
                        else R.drawable.ic_star_outline
                    ),
                    tint = tint,
                    contentDescription = null
                )
            }

            IconButton(
                onClick = onShare
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    tint = tint,
                    contentDescription = null
                )
            }

            IconButton(
                onClick = onOpenUrl
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_open_in_browser),
                    tint = tint,
                    contentDescription = null
                )
            }
        }
    }
}
