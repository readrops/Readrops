package com.readrops.app.compose.feeds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddFeedDialog(
    onDismiss: () -> Unit,
    onValidate: (String) -> Unit,
) {
    var url by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = "Add new feed",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.size(8.dp))

            TextField(
                value = url,
                onValueChange = { url = it }
            )
            
            Spacer(modifier = Modifier.size(8.dp))

            Button(
                onClick = { onValidate(url) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Validate")
            }
        }
    }
}