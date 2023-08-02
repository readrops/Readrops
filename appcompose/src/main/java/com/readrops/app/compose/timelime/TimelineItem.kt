package com.readrops.app.compose.timelime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun TimelineItem(
        onClick: () -> Unit,
) {
    Card(
     //   elevation = 4.card,
        modifier = Modifier.background(Color.White)
            .padding(8.dp)
                .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                   /* Icon(
                        painter = painterResource(id = com.readrops.app.R.drawable.ic_rss_feed_grey),
                        contentDescription = null,
                       // modifier = Modifier.size((MaterialTheme.typography.subtitle2.fontSize.value * 1.5).dp)
                    )*/

//                    Spacer(Modifier.padding(4.dp))

                    Text(
                        text = "feed name",
                        //style = MaterialTheme.typography.
                    )
                }

                Text(
                    text = "Item date",
                   // style = MaterialTheme.typography.subtitle2
                )
            }

            Spacer(Modifier.size(8.dp))

            Text(
                text = "title example",
                //style = MaterialTheme.typography.h5,
            )

            Spacer(Modifier.size(8.dp))

          /*  Image(
                painter = painterResource(id = com.readrops.app.R.drawable.header_background),
                contentDescription = null
            )*/
        }
    }
}