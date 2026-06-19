package com.cemnect.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cemnect.android.domain.model.FlowNode
import com.cemnect.android.domain.model.FlowNodeType
import com.cemnect.android.domain.model.NodeStatus
import com.cemnect.android.ui.theme.NodeBlockedColor
import com.cemnect.android.ui.theme.NodeCompletedColor
import com.cemnect.android.ui.theme.NodeFailedColor
import com.cemnect.android.ui.theme.NodePendingColor
import com.cemnect.android.ui.theme.NodeRunningColor
import com.cemnect.android.ui.theme.NodeSkippedColor

@Composable
fun FlowNodeCard(
    node: FlowNode,
    modifier: Modifier = Modifier,
    isCurrentNode: Boolean = false
) {
    val backgroundColor = getNodeBackgroundColor(node.status)
    val borderColor = if (isCurrentNode) {
        MaterialTheme.colorScheme.primary
    } else {
        backgroundColor
    }
    val borderWidth = if (isCurrentNode) 3.dp else 1.dp

    Column(
        modifier = modifier
            .size(width = 140.dp, height = 80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Node type icon indicator
        NodeTypeIndicator(type = node.type)

        // Node name
        Text(
            text = node.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )

        // Status indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(getStatusColor(node.status), RoundedCornerShape(50))
            )
            Text(
                text = node.status.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NodeTypeIndicator(type: FlowNodeType) {
    val iconText = when (type) {
        FlowNodeType.START -> "▶"
        FlowNodeType.END -> "■"
        FlowNodeType.ACTION -> "⚡"
        FlowNodeType.DECISION -> "◇"
        FlowNodeType.WAIT -> "⏳"
        FlowNodeType.PARALLEL_BRANCH -> "⇉"
        FlowNodeType.MERGE -> "⇆"
    }

    Text(
        text = iconText,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

private fun getNodeBackgroundColor(status: NodeStatus): Color {
    return when (status) {
        NodeStatus.PENDING -> Color.Transparent
        NodeStatus.RUNNING -> NodeRunningColor.copy(alpha = 0.15f)
        NodeStatus.COMPLETED -> NodeCompletedColor.copy(alpha = 0.15f)
        NodeStatus.FAILED -> NodeFailedColor.copy(alpha = 0.15f)
        NodeStatus.BLOCKED -> NodeBlockedColor.copy(alpha = 0.15f)
        NodeStatus.SKIPPED -> NodeSkippedColor.copy(alpha = 0.15f)
    }
}

private fun getStatusColor(status: NodeStatus): Color {
    return when (status) {
        NodeStatus.PENDING -> NodePendingColor
        NodeStatus.RUNNING -> NodeRunningColor
        NodeStatus.COMPLETED -> NodeCompletedColor
        NodeStatus.FAILED -> NodeFailedColor
        NodeStatus.BLOCKED -> NodeBlockedColor
        NodeStatus.SKIPPED -> NodeSkippedColor
    }
}
