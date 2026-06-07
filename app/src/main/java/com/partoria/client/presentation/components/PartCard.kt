package com.partoria.client.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.partoria.client.R
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens
import com.partoria.client.utils.CategoryIcon

@Composable
fun PartCard(
    part: ComputerPart,
    onClick: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: ((Boolean) -> Unit)? = null,
    showEditDelete: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.ElevationDefault),
        shape = RoundedCornerShape(AppDimens.CornerRadiusCard),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.CardWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.PaddingLarge),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(AppDimens.CardIconBoxSize)
                    .clip(RoundedCornerShape(AppDimens.CornerRadiusCard))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                CategoryIcon.getColor(part.category).copy(alpha = 0.2f),
                                CategoryIcon.getColor(part.category).copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIcon.getIcon(part.category),
                    contentDescription = part.category,
                    modifier = Modifier.size(AppDimens.IconSizeLarge),
                    tint = CategoryIcon.getColor(part.category)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextDarkPrimary
                )
                Spacer(modifier = Modifier.height(AppDimens.PaddingMicro))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingMicro),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimens.IconSizeSmall),
                        tint = AppColors.TextDarkSecondary
                    )
                    Text(
                        text = part.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextDarkSecondary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextDarkSecondary
                    )
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimens.IconSizeSmall),
                        tint = AppColors.TextDarkSecondary
                    )
                    Text(
                        text = part.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextDarkSecondary
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                Text(
                    text = "$${String.format("%.2f", part.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingMicro),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimens.IconSizeSmall),
                        tint = AppColors.TextDarkSecondary
                    )
                    Text(
                        text = stringResource(R.string.released_short, part.releaseYear),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextDarkSecondary
                    )
                }
            }

            when {
                showEditDelete && onEdit != null && onDelete != null -> {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(AppDimens.PaddingSmall)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(AppDimens.IconButtonSize)
                                .clip(RoundedCornerShape(AppDimens.CornerRadiusSmall))
                                .background(AppColors.EditButtonBackground)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = AppColors.Primary,
                                modifier = Modifier.size(AppDimens.IconSizeMedium)
                            )
                        }
                        Spacer(modifier = Modifier.height(AppDimens.PaddingMicro))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(AppDimens.IconButtonSize)
                                .clip(RoundedCornerShape(AppDimens.CornerRadiusSmall))
                                .background(AppColors.DeleteButtonBackground)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = AppColors.Error,
                                modifier = Modifier.size(AppDimens.IconSizeMedium)
                            )
                        }
                    }
                }
                onFavoriteClick != null -> {
                    IconButton(
                        onClick = { onFavoriteClick(isFavorite) },
                        modifier = Modifier
                            .size(AppDimens.CardFavoriteButtonSize)
                            .clip(RoundedCornerShape(AppDimens.CornerRadiusButton))
                            .background(
                                if (isFavorite) AppColors.DeleteButtonBackground
                                else Color.Transparent
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) stringResource(R.string.remove_from_favorites) else stringResource(R.string.add_to_favorites),
                            tint = if (isFavorite) AppColors.Error else AppColors.TextDarkSecondary,
                            modifier = Modifier.size(AppDimens.IconSizeNormal)
                        )
                    }
                }
            }
        }
    }
}