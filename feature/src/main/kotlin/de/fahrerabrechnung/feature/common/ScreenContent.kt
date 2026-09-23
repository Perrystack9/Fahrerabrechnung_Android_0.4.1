package de.fahrerabrechnung.feature.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.fahrerabrechnung.core.design.FahrerThemeTokens

@Composable
fun ScreenContent(
    padding: PaddingValues,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FahrerThemeTokens.spacing.lg, vertical = FahrerThemeTokens.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FahrerThemeTokens.spacing.md),
        content = content,
    )
}
