package com.readrops.app.feeds.newfeed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.readrops.app.R
import com.readrops.app.util.components.CompactDropdownBox
import com.readrops.app.util.components.DropdownBoxValue
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.VeryShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Folder

@Composable
fun ParsingResultItem(
    parsingResult: ParsingResultState,
    folders: List<Folder>,
    onCheckedChange: (Boolean) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSelectFolder: (Folder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = animateColorAsState(
            targetValue = if (parsingResult.isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            animationSpec = spring(stiffness = Spring.StiffnessHigh),
            label = "ParsingResult item color animation"
        ).value,
        shape = ShapeDefaults.Medium,
        onClick = { onCheckedChange(!parsingResult.isSelected) },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShortSpacer()

            Icon(
                painter = painterResource(R.drawable.ic_rss_feed_grey),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )

            Column(
                modifier = Modifier.padding(MaterialTheme.spacing.shortSpacing)
            ) {
                Text(
                    text = parsingResult.label ?: parsingResult.url,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (parsingResult.label != null) {
                    VeryShortSpacer()

                    Text(
                        text = parsingResult.url,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (folders.isNotEmpty()) {
                    ShortSpacer()

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_folder_grey),
                            contentDescription = null,
                        )

                        ShortSpacer()

                        CompactDropdownBox(
                            expanded = parsingResult.isExpanded,
                            text = parsingResult.folder?.name.orEmpty(),
                            values = folders.map {
                                DropdownBoxValue(
                                    id = it.id,
                                    text = it.name.orEmpty(),
                                    painter = painterResource(R.drawable.ic_folder_grey)
                                )
                            },
                            onExpandedChange = onExpandedChange,
                            onValueClick = { id -> onSelectFolder(folders.first { it.id == id }) },
                            onDismiss = onDismiss,
                        )
                    }
                }
            }
        }
    }
}