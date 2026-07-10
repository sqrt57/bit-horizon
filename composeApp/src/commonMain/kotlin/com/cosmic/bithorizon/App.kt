package com.cosmic.bithorizon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmic.bithorizon.game.GameState
import com.cosmic.bithorizon.game.TICK_DELTA_SECONDS
import com.cosmic.bithorizon.game.Tier
import com.cosmic.bithorizon.math.Decimal
import kotlinx.coroutines.delay

val TerminalBackground = Color(0xFF0A0A0C)
private val TerminalAccent = Color(0xFF39FF6A)
private val TerminalDim = Color(0xFF6E7681)
private val TerminalPanel = Color(0xFF101014)
private val TerminalMono = FontFamily.Monospace
private const val LOG_HISTORY_LIMIT = 100

private fun tierLabel(tier: Tier): String = when (tier) {
    Tier.TELEMETRY_SENSOR -> "Telemetry Sensor"
    Tier.SUB_PROBE_ARRAY -> "Sub-Probe Array"
    Tier.QUANTUM_RELAY -> "Quantum Relay"
}

private fun paymentUnitLabel(tier: Tier): String = when (tier) {
    Tier.TELEMETRY_SENSOR -> "Data Bits"
    Tier.SUB_PROBE_ARRAY -> "Telemetry Sensors"
    Tier.QUANTUM_RELAY -> "Sub-Probe Arrays"
}

@Composable
fun App() {
    var state by remember { mutableStateOf(GameState()) }
    val log = remember { mutableStateListOf("System initialized. Awaiting telemetry.") }

    fun logEvent(message: String) {
        log.add(0, message)
        if (log.size > LOG_HISTORY_LIMIT) log.removeAt(log.lastIndex)
    }

    fun buyMax(tier: Tier) {
        val amount = state.maxAffordable(tier)
        if (amount.lte(Decimal.ZERO)) return
        val cost = state.costFor(tier, amount)
        state = state.buy(tier, amount)
        logEvent("Purchased $amount ${tierLabel(tier)}(s) for $cost ${paymentUnitLabel(tier)}.")
    }

    fun triggerHorizonShift() {
        if (!state.canHorizonShift()) return
        state = state.horizonShift()
        logEvent("HORIZON SHIFT — physical systems flushed. +1 Tachyon Particle.")
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay((TICK_DELTA_SECONDS * 1000).toLong())
            state = state.tick()
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(TerminalBackground).safeDrawingPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TelemetryMetricsZone(state)
            LogHubZone(entries = log, modifier = Modifier.weight(1f))
            InfrastructureTiersZone(state = state, onBuyMax = ::buyMax)
            ExecutiveCoreZone(
                state = state,
                onManualScan = { state = state.manualScan() },
                onHorizonShift = ::triggerHorizonShift,
            )
        }
    }
}

/** Zone 1: raw Data Bits (scientific format) and current Bits/sec. */
@Composable
private fun TelemetryMetricsZone(state: GameState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "DATA BITS: ${state.dataBits.toExponential(2)}",
            color = TerminalAccent,
            fontFamily = TerminalMono,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
        Text(
            text = "+${state.dataBitsPerSecond().toExponential(2)} / SEC",
            color = TerminalDim,
            fontFamily = TerminalMono,
            fontSize = 14.sp,
        )
    }
}

/** Zone 2: monospaced real-time system event log, newest entry first. */
@Composable
private fun LogHubZone(entries: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(TerminalPanel).padding(8.dp),
    ) {
        items(entries) { entry ->
            Text(
                text = "> $entry",
                color = TerminalAccent,
                fontFamily = TerminalMono,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}

/** Zone 3: vertical stack of the three infrastructure tiers, with counts and a buy-max action. */
@Composable
private fun InfrastructureTiersZone(state: GameState, onBuyMax: (Tier) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (tier in Tier.entries) {
            TierRow(state = state, tier = tier, onBuyMax = { onBuyMax(tier) })
        }
    }
}

@Composable
private fun TierRow(state: GameState, tier: Tier, onBuyMax: () -> Unit) {
    val affordable = state.maxAffordable(tier).gt(Decimal.ZERO)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tierLabel(tier),
                color = Color.White,
                fontFamily = TerminalMono,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Owned: ${state.count(tier)}  ·  Next: ${state.costForNext(tier)} ${paymentUnitLabel(tier)}",
                color = TerminalDim,
                fontFamily = TerminalMono,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Button(
            onClick = onBuyMax,
            enabled = affordable,
            colors = ButtonDefaults.buttonColors(containerColor = TerminalAccent),
        ) {
            Text("BUY MAX", fontFamily = TerminalMono, color = Color.Black)
        }
    }
}

/** Zone 4: the manual-scan / Horizon Shift execution button, morphing once the prestige threshold is hit. */
@Composable
private fun ExecutiveCoreZone(
    state: GameState,
    onManualScan: () -> Unit,
    onHorizonShift: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ready = state.canHorizonShift()
    Button(
        onClick = if (ready) onHorizonShift else onManualScan,
        modifier = modifier.fillMaxWidth().height(72.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (ready) TerminalAccent else TerminalPanel),
    ) {
        Text(
            text = if (ready) "[ HORIZON SHIFT ]" else "MANUAL SCAN",
            fontFamily = TerminalMono,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = if (ready) Color.Black else TerminalAccent,
        )
    }
}
