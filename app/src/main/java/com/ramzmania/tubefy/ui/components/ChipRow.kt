package com.ramzmania.tubefy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverOnAccent
import com.ramzmania.tubefy.ui.theme.DiscoverSurface

/** Horizontally scrolling pill chips with a single selection (home categories, search filters). */
@Composable
fun ChipRow(chips: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(chips) { label ->
            val isSelected = label == selected
            Text(
                text = label,
                color = if (isSelected) DiscoverOnAccent else Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) DiscoverAccent else DiscoverSurface)
                    .border(1.dp, if (isSelected) Color.Transparent else DiscoverBorder, RoundedCornerShape(50))
                    .clickable { onSelect(label) }
                    .padding(horizontal = 20.dp, vertical = 9.dp)
            )
        }
    }
}
