package com.readrops.app.feeds.newfeed

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.account.selection.adaptiveIconPainterResource
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.DropdownBox
import com.readrops.app.util.components.DropdownBoxValue
import com.readrops.app.util.components.LoadingButton
import com.readrops.app.util.components.TextHorizontalDivider
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import org.koin.core.parameter.parametersOf

class NewFeedScreen(val url: String? = null) : AndroidScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<NewFeedScreenModel> { parametersOf(url) }

        val appBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        val state by screenModel.state.collectAsStateWithLifecycle()

        if (state.popScreen) {
            navigator.pop()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.add_feed))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    scrollBehavior = appBarScrollBehavior
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                    .nestedScroll(appBarScrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .animateContentSize()
                    .fillMaxSize(),
            ) {
                OutlinedTextField(
                    value = state.actualUrl,
                    label = { Text(text = stringResource(R.string.enter_url)) },
                    onValueChange = { screenModel.updateUrl(it) },
                    singleLine = true,
                    trailingIcon = {
                        if (state.actualUrl.isNotEmpty()) {
                            IconButton(
                                onClick = { screenModel.updateUrl("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        screenModel.validate()
                    }),
                    isError = state.isURLError,
                    supportingText = { Text(state.urlError?.errorText().orEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                )

                ShortSpacer()

                TextHorizontalDivider(text = stringResource(R.string.account))

                ShortSpacer()

                DropdownBox(
                    expanded = state.isAccountDropdownExpanded,
                    text = state.selectedAccount?.name.orEmpty(),
                    label = stringResource(R.string.choose_account),
                    painter = if (state.selectedAccount != null) {
                        adaptiveIconPainterResource(state.selectedAccount!!.type!!.iconRes)
                    } else null,
                    values = state.accounts.map {
                        DropdownBoxValue(
                            id = it.id,
                            text = it.name.orEmpty(),
                            painter = adaptiveIconPainterResource(it.type!!.iconRes)
                        )
                    },
                    onExpandedChange = { screenModel.updateAccountDropDownExpandStatus(it) },
                    onValueClick = { id -> screenModel.updateSelectedAccount(state.accounts.first { it.id == id }) },
                    onDismiss = { screenModel.updateAccountDropDownExpandStatus(false) },
                    modifier = Modifier.fillMaxWidth()
                )

                ShortSpacer()

                DropdownBox(
                    expanded = state.isFoldersDropdownExpanded,
                    text = state.selectedFolder?.name.orEmpty(),
                    label = stringResource(R.string.choose_folder),
                    painter = if (state.selectedFolder != null) {
                        painterResource(R.drawable.ic_folder_grey)
                    } else null,
                    enabled = state.folders.isNotEmpty(),
                    values = state.folders.map {
                        DropdownBoxValue(
                            id = it.id,
                            text = it.name.orEmpty(),
                            painter = painterResource(R.drawable.ic_folder_grey)
                        )
                    },
                    onExpandedChange = { screenModel.updateFolderDropdownExpandStatus(it) },
                    onValueClick = { id -> screenModel.updateSelectedFolder(state.folders.first { it.id == id }) },
                    onDismiss = { screenModel.updateFolderDropdownExpandStatus(false) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.parsingResults.isNotEmpty()) {
                    LargeSpacer()

                    TextHorizontalDivider(
                        text = stringResource(R.string.feeds) + " " + stringResource(
                            R.string.selected,
                            state.selectedResultsCount
                        )
                    )

                    ShortSpacer()

                    for (parsingResult in state.parsingResults) {
                        ParsingResultItem(
                            parsingResult = parsingResult,
                            folders = state.folders,
                            onExpandedChange = {
                                screenModel.updateParsingResultExpandedState(
                                    parsingResult = parsingResult,
                                    isExpanded = it
                                )
                            },
                            onSelectFolder = { folder ->
                                screenModel.updateParsingResultFolder(
                                    parsingResult = parsingResult,
                                    folder = folder
                                )
                            },
                            onCheckedChange = {
                                screenModel.updateParsingResultCheckedState(
                                    parsingResult
                                )
                            },
                            onDismiss = {
                                screenModel.updateParsingResultExpandedState(
                                    parsingResult = parsingResult,
                                    isExpanded = false
                                )
                            },
                            error = parsingResult.error,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ShortSpacer()
                    }
                }

                if (state.error != null) {
                    MediumSpacer()

                    Text(
                        text = state.error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                MediumSpacer()

                LoadingButton(
                    text = if (state.selectedResultsCount > 0) {
                        stringResource(R.string.add_selected_feeds, state.selectedResultsCount)
                    } else {
                        stringResource(id = R.string.validate)
                    },
                    isLoading = state.isLoading,
                    onClick = { screenModel.validate() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}