package com.cemnect.android.ui.screens.execution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cemnect.android.domain.engine.FlowGraphStateEngine
import com.cemnect.android.domain.model.ExecutionStatus
import com.cemnect.android.domain.model.FlowEdge
import com.cemnect.android.domain.model.FlowNode
import com.cemnect.android.domain.model.FlowNodeType
import com.cemnect.android.domain.model.NodeStatus
import com.cemnect.android.domain.model.Task
import com.cemnect.android.ui.components.LiveFlowGraphPanel

@Composable
fun TaskExecutionScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Create the flow graph engine (in real app, this would be injected via Hilt)
    val flowGraphEngine = remember { FlowGraphStateEngine() }
    val flowState by flowGraphEngine.flowGraphState.collectAsState()

    // Initialize with demo task data
    LaunchedEffect(taskId) {
        val demoTask = createDemoTask(taskId)
        flowGraphEngine.initialize(demoTask)
        
        // Simulate task execution for demo purposes
        simulateTaskExecution(flowGraphEngine)
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TOP 25% - Live Flow Graph Panel (reserved space as per requirement)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                LiveFlowGraphPanel(
                    flowGraphEngine = flowGraphEngine,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // BOTTOM 75% - Task execution details and controls
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.75f)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Task Execution Details",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Task info
                flowState.task?.let { task ->
                    Text(
                        text = "Task: ${task.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current node info
                flowState.execution?.currentNodeId?.let { currentNodeId ->
                    flowState.nodes[currentNodeId]?.let { node ->
                        Text(
                            text = "Current Step: ${node.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Block reason display
                if (flowState.hasBlockingError()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                MaterialTheme.shapes.medium
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Execution Blocked\n${flowState.blockingErrorMessage() ?: "Unknown error"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Control buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val execution = flowState.execution
                    
                    when (execution?.status) {
                        ExecutionStatus.NOT_STARTED,
                        ExecutionStatus.COMPLETED,
                        ExecutionStatus.FAILED,
                        ExecutionStatus.CANCELLED -> {
                            Button(
                                onClick = { 
                                    flowGraphEngine.reset()
                                    flowGraphEngine.startExecution()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Start Task")
                            }
                        }
                        ExecutionStatus.RUNNING -> {
                            Button(
                                onClick = { flowGraphEngine.pauseExecution() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Pause")
                            }
                        }
                        ExecutionStatus.PAUSED -> {
                            Button(
                                onClick = { flowGraphEngine.resumeExecution() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Resume")
                            }
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Back to Home")
                    }
                }
            }
        }
    }
}

/**
 * Create a demo task for visualization purposes.
 */
private fun createDemoTask(taskId: String): Task {
    val nodes = listOf(
        FlowNode(
            id = "start",
            name = "Start",
            type = FlowNodeType.START,
            status = NodeStatus.PENDING
        ),
        FlowNode(
            id = "check-permission",
            name = "Check Permission",
            type = FlowNodeType.DECISION,
            status = NodeStatus.PENDING,
            description = "Verify app has required permissions"
        ),
        FlowNode(
            id = "open-app",
            name = "Open App",
            type = FlowNodeType.ACTION,
            status = NodeStatus.PENDING,
            description = "Launch target application"
        ),
        FlowNode(
            id = "wait-load",
            name = "Wait for Load",
            type = FlowNodeType.WAIT,
            status = NodeStatus.PENDING,
            description = "Wait for UI to stabilize"
        ),
        FlowNode(
            id = "find-element",
            name = "Find Element",
            type = FlowNodeType.ACTION,
            status = NodeStatus.PENDING,
            description = "Locate target UI element"
        ),
        FlowNode(
            id = "click-element",
            name = "Click Element",
            type = FlowNodeType.ACTION,
            status = NodeStatus.PENDING,
            description = "Perform click action"
        ),
        FlowNode(
            id = "verify-result",
            name = "Verify Result",
            type = FlowNodeType.DECISION,
            status = NodeStatus.PENDING,
            description = "Validate expected outcome"
        ),
        FlowNode(
            id = "end",
            name = "End",
            type = FlowNodeType.END,
            status = NodeStatus.PENDING
        )
    )

    val edges = listOf(
        FlowEdge("e1", "start", "check-permission"),
        FlowEdge("e2", "check-permission", "open-app", "Has Permission"),
        FlowEdge("e3", "open-app", "wait-load"),
        FlowEdge("e4", "wait-load", "find-element"),
        FlowEdge("e5", "find-element", "click-element"),
        FlowEdge("e6", "click-element", "verify-result"),
        FlowEdge("e7", "verify-result", "end", "Success")
    )

    return Task(
        id = taskId,
        name = "Demo Automation Task",
        description = "A sample task demonstrating the live flow graph visualization",
        flowNodes = nodes,
        flowEdges = edges
    )
}

/**
 * Simulate task execution for demo purposes.
 * In production, this would be replaced with actual task execution logic.
 */
private suspend fun simulateTaskExecution(engine: FlowGraphStateEngine) {
    // Start execution
    kotlinx.coroutines.delay(1000)
    engine.startExecution()

    // Move through nodes with delays to simulate execution
    val nodeOrder = listOf(
        "start",
        "check-permission",
        "open-app",
        "wait-load",
        "find-element",
        "click-element",
        "verify-result",
        "end"
    )

    for (nodeId in nodeOrder) {
        kotlinx.coroutines.delay(2000)
        engine.moveToNode(nodeId)
    }

    // Complete after reaching end
    kotlinx.coroutines.delay(500)
    engine.completeExecution()
}
