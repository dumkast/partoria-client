package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.partoria.client.R
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens
import com.partoria.client.utils.CategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartDetailScreen(
    partId: Int,
    partsViewModel: PartsViewModel,
    onBack: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit
) {
    var part by remember { mutableStateOf<ComputerPart?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(partId) {
        partsViewModel.loadPartDetails(partId) { result ->
            part = result
            isLoading = false
        }
        partsViewModel.isFavorite(partId) { fav ->
            isFavorite = fav
        }
    }

    Scaffold(
        containerColor = AppColors.BackgroundEnd,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.part_details),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = AppColors.TextPrimary)
                    }
                },
                actions = {
                    if (part != null) {
                        IconButton(
                            onClick = {
                                onFavoriteClick(isFavorite)
                                isFavorite = !isFavorite
                            }
                        ) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.favorites),
                                tint = if (isFavorite) AppColors.Error else AppColors.TextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundStart
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(AppColors.BackgroundStart, AppColors.BackgroundEnd)
                    )
                )
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppColors.Primary
                )
            } else if (part == null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(AppDimens.CardWidthPartNotFound)
                        .clip(RoundedCornerShape(AppDimens.CornerRadiusXLarge)),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.CardBackground
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.PaddingXXLarge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimens.IconSizeXLarge),
                            tint = AppColors.TextHint
                        )
                        Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                        Text(
                            text = stringResource(R.string.part_not_found),
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextPrimary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(AppDimens.PaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.PaddingMedium)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppDimens.CornerRadiusLarge),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.CardBackground
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppDimens.PaddingXLarge),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(AppDimens.AvatarSize)
                                        .clip(RoundedCornerShape(AppDimens.CornerRadiusAvatar))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    CategoryIcon.getColor(part?.category ?: "").copy(alpha = 0.2f),
                                                    CategoryIcon.getColor(part?.category ?: "").copy(alpha = 0.05f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryIcon.getIcon(part?.category ?: ""),
                                        contentDescription = null,
                                        modifier = Modifier.size(AppDimens.AvatarIconSize),
                                        tint = CategoryIcon.getColor(part?.category ?: "")
                                    )
                                }
                                Spacer(modifier = Modifier.height(AppDimens.PaddingXLarge))
                                Text(
                                    text = part?.name ?: "",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(AppDimens.PaddingMicro))
                                Text(
                                    text = "${part?.brand} • ${part?.category}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(AppDimens.CornerRadiusButton)),
                                    color = AppColors.Primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "$${String.format("%.2f", part?.price)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary,
                                        modifier = Modifier.padding(horizontal = AppDimens.PaddingXLarge, vertical = AppDimens.PaddingSmall)
                                    )
                                }
                                Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingMicro)
                                ) {
                                    Icon(
                                        Icons.Default.Sell,
                                        contentDescription = null,
                                        modifier = Modifier.size(AppDimens.IconSizeSmall),
                                        tint = AppColors.TextHint
                                    )
                                    Text(
                                        text = "${stringResource(R.string.released)} ${part?.releaseYear}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.TextHint
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppDimens.CornerRadiusLarge),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.CardBackground
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppDimens.PaddingXLarge)
                            ) {
                                Text(
                                    text = stringResource(R.string.specifications),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))
                                Text(
                                    text = part?.specs ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextSecondary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    if (part?.details?.isNotEmpty() == true) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppDimens.CornerRadiusLarge),
                                colors = CardDefaults.cardColors(
                                    containerColor = AppColors.CardBackground
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppDimens.PaddingXLarge)
                                ) {
                                    Text(
                                        text = stringResource(R.string.technical_details),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))
                                    part?.details?.forEachIndexed { index, detail ->
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = detail.specification,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = AppColors.TextPrimary
                                                )
                                                Text(
                                                    text = detail.value,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AppColors.Primary
                                                )
                                            }
                                            if (index < (part?.details?.size ?: 0) - 1) {
                                                Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                                                Divider(
                                                    color = AppColors.BorderUnfocused,
                                                    modifier = Modifier.padding(vertical = AppDimens.PaddingMicro)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}