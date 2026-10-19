package com.ramzmania.tubefy.ui.components.screen.home

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.local.PodcastLanguages
import com.ramzmania.tubefy.data.remote.PodcastShow
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverOnAccent
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val ALL_PODCASTS = "All"

/**
 * Home's Podcasts chip: "All" (YouTube Music's podcasts page) plus per-language podcast shows.
 * Built-in Indian languages are always there; users can add more with "+" (only if YouTube has
 * podcasts for it) and long-press their own languages to remove them.
 */
@Composable
fun PodcastsContent(viewModel: TubeFyViewModel) {
    val context = LocalContext.current
    val customLanguages by viewModel.customPodcastLanguages.collectAsState()
    var selected by rememberSaveable { mutableStateOf(ALL_PODCASTS) }
    var showAddDialog by remember { mutableStateOf(false) }
    var removeTarget by remember { mutableStateOf<String?>(null) }

    // A removed language can't stay selected
    if (selected != ALL_PODCASTS && selected !in PodcastLanguages.DEFAULTS && selected !in customLanguages) {
        selected = ALL_PODCASTS
    }

    if (showAddDialog) {
        AddLanguageDialog(
            viewModel = viewModel,
            onAdded = { language ->
                showAddDialog = false
                selected = PodcastLanguages.DEFAULTS.plus(customLanguages)
                    .firstOrNull { it.equals(language, ignoreCase = true) }
                    ?: language.trim().replaceFirstChar { it.uppercase() }
                Toast.makeText(context, "Added $selected podcasts", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddDialog = false }
        )
    }

    removeTarget?.let { language ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            containerColor = DialogColor,
            title = { Text("Remove $language?", color = Color.White) },
            text = { Text("$language podcasts will no longer show here. You can add it again with +.", color = DiscoverTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removePodcastLanguage(language)
                    removeTarget = null
                }) { Text("Remove", color = DiscoverAccent) }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) { Text("Cancel", color = DiscoverTextSecondary) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LanguageChipRow(
            languages = listOf(ALL_PODCASTS) + PodcastLanguages.DEFAULTS + customLanguages,
            customLanguages = customLanguages,
            selected = selected,
            onSelect = { selected = it },
            onLongPressCustom = { removeTarget = it },
            onAdd = { showAddDialog = true }
        )
        Box(modifier = Modifier.weight(1f)) {
            if (selected == ALL_PODCASTS) {
                HomeCategoryContent(viewModel = viewModel, category = TubeFyViewModel.PodcastsCategory)
            } else {
                PodcastShowsGrid(viewModel = viewModel, language = selected)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LanguageChipRow(
    languages: List<String>,
    customLanguages: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onLongPressCustom: (String) -> Unit,
    onAdd: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(languages, key = { it }) { language ->
            val isSelected = language == selected
            val isCustom = language in customLanguages
            Text(
                text = language,
                color = if (isSelected) DiscoverOnAccent else Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) DiscoverAccent else DiscoverSurface)
                    .border(1.dp, if (isSelected) Color.Transparent else DiscoverBorder, RoundedCornerShape(50))
                    .combinedClickable(
                        onClick = { onSelect(language) },
                        onLongClick = if (isCustom) {
                            {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPressCustom(language)
                            }
                        } else null
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            )
        }
        item(key = "add") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DiscoverSurface)
                    .border(1.dp, DiscoverAccent.copy(alpha = 0.6f), CircleShape)
                    .clickable(onClick = onAdd)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add a language", tint = DiscoverAccent, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PodcastShowsGrid(viewModel: TubeFyViewModel, language: String) {
    val allShows by viewModel.podcastShows.collectAsState()
    val refreshingLanguages by viewModel.refreshingPodcasts.collectAsState()
    val refreshing = language.lowercase() in refreshingLanguages
    val pullState = rememberPullToRefreshState()
    LaunchedEffect(language) { viewModel.loadPodcastsFor(language) }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { viewModel.refreshPodcastsFor(language) },
        state = pullState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = refreshing,
                containerColor = DiscoverSurface,
                color = DiscoverAccent,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        PodcastShowsState(viewModel, language, allShows[language.lowercase()])
    }
}

@Composable
private fun PodcastShowsState(viewModel: TubeFyViewModel, language: String, result: Resource<List<PodcastShow>>?) {
    val navController = LocalNavController.current
    when (result) {
        is Resource.Success -> {
            val shows = result.data.orEmpty()
            if (shows.isEmpty()) {
                CenteredNote("No $language podcasts found")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(shows, key = { it.playlistId }) { show ->
                        PodcastShowCard(show) {
                            navController.navigate(
                                NavigationItem.PlayList.createRoute(
                                    show.playlistId,
                                    URLEncoder.encode(show.title, StandardCharsets.UTF_8.toString()),
                                    URLEncoder.encode(show.thumbnail.ifBlank { "nourl" }, StandardCharsets.UTF_8.toString())
                                )
                            ) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }
        }

        is Resource.DataError -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // scrollable so it can be pulled to refresh
                .padding(top = 48.dp)
        ) {
            Text("Couldn't load $language podcasts", color = Color.White, fontSize = 16.sp)
            TextButton(onClick = { viewModel.loadPodcastsFor(language, forceRefresh = true) }) {
                Text("Retry", color = DiscoverAccent, fontSize = 16.sp)
            }
        }

        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DiscoverAccent)
        }
    }
}

@Composable
private fun PodcastShowCard(show: PodcastShow, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(show.thumbnail.ifBlank { null } ?: R.drawable.placeholder)
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = show.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
        )
        Text(
            text = show.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (show.author.isNotBlank()) {
            Text(
                text = show.author,
                color = DiscoverTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
        }
    }
}

/** Type a language; it's added only if YouTube has podcasts in it. */
@Composable
private fun AddLanguageDialog(
    viewModel: TubeFyViewModel,
    onAdded: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var notFound by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val language = name.trim()
        if (language.isEmpty() || searching) return
        searching = true
        notFound = null
        viewModel.addPodcastLanguage(language) { added ->
            searching = false
            if (added) onAdded(language) else notFound = language
        }
    }

    AlertDialog(
        onDismissRequest = { if (!searching) onDismiss() },
        containerColor = DialogColor,
        title = { Text("Add a language", color = Color.White) },
        text = {
            Column {
                Text("We'll look for podcasts in this language.", color = DiscoverTextSecondary, fontSize = 14.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        notFound = null
                    },
                    placeholder = { Text("e.g. Odia, Urdu, English", color = DiscoverTextSecondary) },
                    singleLine = true,
                    enabled = !searching,
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiscoverAccent,
                        unfocusedBorderColor = DiscoverBorder,
                        focusedContainerColor = DiscoverSurface,
                        unfocusedContainerColor = DiscoverSurface,
                        disabledContainerColor = DiscoverSurface,
                        cursorColor = DiscoverAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                when {
                    searching -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Text("Looking for podcasts…", color = DiscoverTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp))
                    }
                    notFound != null -> Text(
                        "No podcasts found for \"$notFound\". Try another language.",
                        color = DiscoverAccent,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { submit() }, enabled = name.isNotBlank() && !searching) {
                Text("Add", color = if (name.isNotBlank() && !searching) DiscoverAccent else DiscoverTextSecondary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !searching) { Text("Cancel", color = DiscoverTextSecondary) }
        }
    )
}

@Composable
private fun CenteredNote(text: String) {
    // Scrollable so an empty result can still be pulled to refresh; sized to the screen to stay centered
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val height = maxHeight
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .height(height)
        ) {
            Text(text = text, color = DiscoverTextSecondary, fontSize = 16.sp)
        }
    }
}

private val DialogColor = Color(0xFF161314)
