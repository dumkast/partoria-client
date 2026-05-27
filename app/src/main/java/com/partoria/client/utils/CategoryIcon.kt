package com.partoria.client.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.partoria.client.R

object CategoryIcon {

    @Composable
    fun getIcon(category: String): ImageVector {
        return when (category) {
            "CPU" -> ImageVector.vectorResource(id = R.drawable.cpu)
            "GPU" -> ImageVector.vectorResource(id = R.drawable.gpu)
            "RAM" -> ImageVector.vectorResource(id = R.drawable.memory_stick)
            "Motherboard" -> ImageVector.vectorResource(id = R.drawable.circuit_board)
            "Storage" -> ImageVector.vectorResource(id = R.drawable.hard_drive)
            else -> Icons.Default.Settings
        }
    }

    fun getColor(category: String): Color {
        return when (category) {
            "CPU" -> Color(0xFF2196F3)
            "GPU" -> Color(0xFF4CAF50)
            "RAM" -> Color(0xFFFF9800)
            "Motherboard" -> Color(0xFF9C27B0)
            "Storage" -> Color(0xFF00BCD4)
            else -> Color(0xFF757575)
        }
    }
}