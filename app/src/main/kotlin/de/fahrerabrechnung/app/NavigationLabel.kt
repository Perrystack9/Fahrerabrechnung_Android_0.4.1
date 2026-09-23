package de.fahrerabrechnung.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

/** One measurement shared by every item, including when pixel rounding differs between cells. */
@Composable
internal fun navigationLabelStyle(allLabels: List<String>, availableWidthPx: Int): TextStyle {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val baseStyle = MaterialTheme.typography.labelMedium
    return remember(availableWidthPx, density.density, density.fontScale, baseStyle, measurer, allLabels) {
        val size = (48 downTo 32).firstOrNull { quarterPoints ->
            allLabels.all { text ->
                measurer.measure(
                    text = text,
                    style = baseStyle.copy(fontSize = (quarterPoints / 4f).sp),
                    maxLines = 1,
                    softWrap = false,
                ).size.width <= availableWidthPx
            }
        }?.let { (it / 4f).sp } ?: 8.sp
        baseStyle.copy(fontSize = size)
    }
}

@Composable
internal fun NavigationLabel(label: String, route: String, style: TextStyle) {
    Text(text = label, style = style, maxLines = 1, softWrap = false, modifier = Modifier.testTag("nav_label_$route"))
}
