package de.fahrerabrechnung.core.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fahrerabrechnung.core.R

object FahrerBrand {
    const val NAME = "FahrerTagebuch"
    const val CLAIM = "Touren. Zeiten. Übersicht."
}

/** A square vector symbol; the wordmark is accessible text, never a stretched bitmap. */
@Composable
fun BrandLockup(modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val symbol = if (dark) R.drawable.ft_symbol_dark else R.drawable.ft_symbol
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(painterResource(symbol), contentDescription = null, modifier = Modifier.size(40.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                buildAnnotatedString {
                    append("Fahrer")
                    withStyle(SpanStyle(color = BrandColors.Petrol)) { append("Tagebuch") }
                },
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = if (dark) BrandColors.White else BrandColors.Navy,
                maxLines = 1,
                softWrap = false,
            )
            Text(FahrerBrand.CLAIM, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
