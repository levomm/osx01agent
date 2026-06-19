package com.cemnect.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cemnect.android.domain.engine.FlowGraphStateEngine
import com.cemnect.android.domain.model.NodeStatus
import com.cemnect.android.ui.theme.EdgeActiveColor
import com.cemnect.android.ui.theme.EdgeCompletedColor
import com.cemnect.android.ui.theme.EdgeInactiveColor

@Composable
fun LiveFlowGraphPanel(
    flowGraphEngine: FlowGraphStateEngine,
    modifier: Modifier = Modifier
) {
    val flowState by flowGraphEngine.flowGraphState.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header with status
            FlowGraphHeader(flowState = flowState)

            Spacer(modifier = Modifier.height(8.dp))

            // Node visualization area
            if (flowState.nodes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No task loaded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Horizontal scrollable node layout
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(
                        items = flowState.orderedNodes(),
                        key = { it.id }
                    ) { node ->
                        FlowNodeCard(
                            node = node,
                            isCurrentNode = node.id == flowState.execution?.currentNodeId
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowGraphHeader(
    flowState: com.cemnect.android.domain.engine.FlowGraphState,
    modifier: Modifier = Modifier
) {
    val execution = flowState.execution

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Title and progress row
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Flow Graph",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            // Progress indicator
            if (execution != null) {
                CircularProgressIndicator(
                    progress = { execution.progress },
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = when {
                        execution.status == com.cemnect.android.domain.model.ExecutionStatus.COMPLETED -> 
                            EdgeCompletedColor
                        execution.status == com.cemnect.android.domain.model.ExecutionStatus.FAILED -> 
                            com.cemnect.android.ui.theme.NodeFailedColor
                        else -> EdgeActiveColor
                    }
                )
            }
        }

        // Status message or block reason
        if (flowState.hasBlockingError()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⚠️ Blocked: ${flowState.blockingErrorMessage() ?: "Unknown error"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            execution?.status?.let { status ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Status: ${status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (status) {
                        com.cemnect.android.domain.model.ExecutionStatus.RUNNING -> 
                            EdgeActiveColor
                        com.cemnect.android.domain.model.ExecutionStatus.COMPLETED -> 
                            EdgeCompletedColor
                        com.cemnect.android.domain.model.ExecutionStatus.FAILED -> 
                            com.cemnect.android.ui.theme.NodeFailedColor
                        else -> 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
