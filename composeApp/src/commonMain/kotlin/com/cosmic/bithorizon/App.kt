package com.cosmic.bithorizon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

val TerminalBackground = Color(0xFF0A0A0C)

@Composable
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(TerminalBackground),
            contentAlignment = Alignment.Center,
        ) {
            // TODO: Zone 1 - Telemetry Metrics
            // TODO: Zone 2 - The Log Hub
            // TODO: Zone 3 - Infrastructure Tiers
            // TODO: Zone 4 - Executive Core
            Text("BitHorizon", color = Color.White)
        }
    }
}
