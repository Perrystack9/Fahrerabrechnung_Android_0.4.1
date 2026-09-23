package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Every item fits the longest label, so all five labels use the same size. */
@Composable
internal fun NavigationLabel(label: String, allLabels: List<String>, route: String) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val baseStyle = MaterialTheme.typography.labelMedium
    BoxWithConstraints {
        val availableWidth = with(density) { (maxWidth - 2.dp).roundToPx() }
        val size = remember(availableWidth, density.density, density.fontScale, baseStyle, measurer, allLabels) {
            (48 downTo 32).firstOrNull { quarterPoints ->
                allLabels.all { text ->
                    measurer.measure(
                        text = text,
                        style = baseStyle.copy(fontSize = (quarterPoints / 4f).sp),
                        maxLines = 1,
                        softWrap = false,
                    ).size.width <= availableWidth
                }
            }?.let { (it / 4f).sp } ?: 8.sp
        }
        Text(
            text = label,
            style = baseStyle.copy(fontSize = size),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.testTag("nav_label_$route"),
        )
    }
}
