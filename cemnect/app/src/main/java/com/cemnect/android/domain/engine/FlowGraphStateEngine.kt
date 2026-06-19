package com.cemnect.android.domain.engine

import com.cemnect.android.domain.model.ExecutionStatus
import com.cemnect.android.domain.model.FlowEdge
import com.cemnect.android.domain.model.FlowNode
import com.cemnect.android.domain.model.FlowNodeType
import com.cemnect.android.domain.model.NodeStatus
import com.cemnect.android.domain.model.Task
import com.cemnect.android.domain.model.TaskExecution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Engine that manages the state of the flow graph during task execution.
 * This is the core engine that drives the Live Flow Graph visualization.
 */
class FlowGraphStateEngine {

    private val _flowGraphState = MutableStateFlow(FlowGraphState())
    val flowGraphState: StateFlow<FlowGraphState> = _flowGraphState.asStateFlow()

    /**
     * Initialize the flow graph with a task definition.
     */
    fun initialize(task: Task) {
        _flowGraphState.update {
            FlowGraphState(
                task = task,
                nodes = task.flowNodes.associateBy { it.id },
                edges = task.flowEdges.associateBy { it.id },
                execution = TaskExecution(
                    taskId = task.id,
                    status = ExecutionStatus.NOT_STARTED,
                    currentNodeId = null,
                    progress = 0f,
                    startTime = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Start task execution.
     */
    fun startExecution() {
        _flowGraphState.update { currentState ->
            val execution = currentState.execution?.copy(
                status = ExecutionStatus.RUNNING,
                startTime = System.currentTimeMillis()
            ) ?: return@update currentState

            // Find the start node and set it to running
            val startNode = currentState.nodes.values.find { it.type == FlowNodeType.START }
            val updatedNodes = if (startNode != null) {
                currentState.nodes + (startNode.id to startNode.copy(
                    status = NodeStatus.RUNNING,
                    startedAt = System.currentTimeMillis()
                ))
            } else {
                currentState.nodes
            }

            currentState.copy(
                execution = execution,
                nodes = updatedNodes
            )
        }
    }

    /**
     * Move execution to a specific node.
     */
    fun moveToNode(nodeId: String) {
        _flowGraphState.update { currentState ->
            val now = System.currentTimeMillis()
            
            // Mark previous current node as completed if exists
            val previousNodeId = currentState.execution?.currentNodeId
            val updatedNodesAfterCompletion = if (previousNodeId != null) {
                currentState.nodes[previousNodeId]?.let { prevNode ->
                    currentState.nodes + (previousNodeId to prevNode.copy(
                        status = NodeStatus.COMPLETED,
                        completedAt = now
                    ))
                } ?: currentState.nodes
            } else {
                currentState.nodes
            }

            // Set new current node to running
            val currentNode = updatedNodesAfterCompletion[nodeId]
            val updatedNodes = if (currentNode != null) {
                updatedNodesAfterCompletion + (nodeId to currentNode.copy(
                    status = NodeStatus.RUNNING,
                    startedAt = now
                ))
            } else {
                updatedNodesAfterCompletion
            }

            // Update edge activity
            val activeEdge = currentState.edges.values.find { 
                it.sourceNodeId == previousNodeId && it.targetNodeId == nodeId 
            }
            val updatedEdges = if (activeEdge != null) {
                currentState.edges + (activeEdge.id to activeEdge.copy(isActive = true))
            } else {
                currentState.edges
            }

            // Calculate progress
            val totalNodes = currentState.nodes.size
            val completedNodes = updatedNodes.values.count { 
                it.status == NodeStatus.COMPLETED 
            }
            val progress = completedNodes.toFloat() / totalNodes.coerceAtLeast(1)

            currentState.copy(
                nodes = updatedNodes,
                edges = updatedEdges,
                execution = currentState.execution?.copy(
                    currentNodeId = nodeId,
                    progress = progress
                )
            )
        }
    }

    /**
     * Mark the current node as failed with an error message.
     */
    fun failCurrentNode(errorMessage: String) {
        _flowGraphState.update { currentState ->
            val currentNodeId = currentState.execution?.currentNodeId ?: return@update currentState
            val now = System.currentTimeMillis()

            val currentNode = currentState.nodes[currentNodeId] ?: return@update currentState
            val updatedNodes = currentState.nodes + (currentNodeId to currentNode.copy(
                status = NodeStatus.FAILED,
                completedAt = now,
                errorMessage = errorMessage
            ))

            // Find downstream nodes and mark them as blocked
            val blockedNodes = findDownstreamNodes(currentNodeId, currentState.edges)
                .filterNot { it == currentNodeId }
                .associateWith { nodeId ->
                    currentState.nodes[nodeId]?.copy(status = NodeStatus.BLOCKED)
                }
                .filterValues { it != null }
                .mapValues { it.value!! }

            currentState.copy(
                nodes = updatedNodes + blockedNodes,
                execution = currentState.execution?.copy(
                    status = ExecutionStatus.FAILED,
                    errorMessage = errorMessage
                )
            )
        }
    }

    /**
     * Pause task execution.
     */
    fun pauseExecution() {
        _flowGraphState.update { currentState ->
            val execution = currentState.execution ?: return@update currentState
            if (execution.status != ExecutionStatus.RUNNING) return@update currentState

            currentState.copy(
                execution = execution.copy(status = ExecutionStatus.PAUSED)
            )
        }
    }

    /**
     * Resume task execution.
     */
    fun resumeExecution() {
        _flowGraphState.update { currentState ->
            val execution = currentState.execution ?: return@update currentState
            if (execution.status != ExecutionStatus.PAUSED) return@update currentState

            currentState.copy(
                execution = execution.copy(status = ExecutionStatus.RUNNING)
            )
        }
    }

    /**
     * Complete task execution successfully.
     */
    fun completeExecution() {
        _flowGraphState.update { currentState ->
            val now = System.currentTimeMillis()
            
            // Mark all remaining nodes as skipped or completed
            val updatedNodes = currentState.nodes.mapValues { (_, node) ->
                when (node.status) {
                    NodeStatus.PENDING, NodeStatus.BLOCKED -> node.copy(status = NodeStatus.SKIPPED)
                    NodeStatus.RUNNING -> node.copy(
                        status = NodeStatus.COMPLETED,
                        completedAt = now
                    )
                    else -> node
                }
            }

            currentState.copy(
                nodes = updatedNodes,
                execution = currentState.execution?.copy(
                    status = ExecutionStatus.COMPLETED,
                    progress = 1f,
                    endTime = now
                )
            )
        }
    }

    /**
     * Cancel task execution.
     */
    fun cancelExecution() {
        _flowGraphState.update { currentState ->
            val now = System.currentTimeMillis()
            
            val updatedNodes = currentState.nodes.mapValues { (_, node) ->
                when (node.status) {
                    NodeStatus.RUNNING -> node.copy(
                        status = NodeStatus.SKIPPED,
                        completedAt = now
                    )
                    NodeStatus.PENDING, NodeStatus.BLOCKED -> node.copy(status = NodeStatus.SKIPPED)
                    else -> node
                }
            }

            currentState.copy(
                nodes = updatedNodes,
                execution = currentState.execution?.copy(
                    status = ExecutionStatus.CANCELLED,
                    endTime = now
                )
            )
        }
    }

    /**
     * Get the reason why execution is blocked (if any).
     */
    fun getBlockReason(): String? {
        val state = _flowGraphState.value
        val failedNode = state.nodes.values.find { it.status == NodeStatus.FAILED }
        return failedNode?.errorMessage
    }

    /**
     * Find all downstream nodes from a given node ID.
     */
    private fun findDownstreamNodes(
        nodeId: String,
        edges: Map<String, FlowEdge>,
        visited: MutableSet<String> = mutableSetOf()
    ): Set<String> {
        if (visited.contains(nodeId)) return emptySet()
        visited.add(nodeId)

        val directChildren = edges.values
            .filter { it.sourceNodeId == nodeId }
            .map { it.targetNodeId }

        return directChildren.toSet() + directChildren.flatMap { 
            findDownstreamNodes(it, edges, visited) 
        }
    }

    /**
     * Reset the flow graph state.
     */
    fun reset() {
        _flowGraphState.update { currentState ->
            val resetNodes = currentState.nodes.mapValues { (_, node) ->
                node.copy(
                    status = NodeStatus.PENDING,
                    startedAt = null,
                    completedAt = null,
                    errorMessage = null
                )
            }

            val resetEdges = currentState.edges.mapValues { (_, edge) ->
                edge.copy(isActive = false)
            }

            currentState.copy(
                nodes = resetNodes,
                edges = resetEdges,
                execution = currentState.execution?.copy(
                    status = ExecutionStatus.NOT_STARTED,
                    currentNodeId = null,
                    progress = 0f,
                    endTime = null,
                    errorMessage = null
                )
            )
        }
    }
}

/**
 * Represents the complete state of the flow graph.
 */
data class FlowGraphState(
    val task: Task? = null,
    val nodes: Map<String, FlowNode> = emptyMap(),
    val edges: Map<String, FlowEdge> = emptyMap(),
    val execution: TaskExecution? = null
) {
    /**
     * Get the list of nodes in a deterministic order for rendering.
     */
    fun orderedNodes(): List<FlowNode> {
        return nodes.values.sortedWith(
            compareBy<FlowNode> { 
                when (it.type) {
                    FlowNodeType.START -> 0
                    FlowNodeType.END -> 1000
                    else -> 500
                }
            }.thenBy { it.startedAt ?: Long.MAX_VALUE }
        )
    }

    /**
     * Get active edges (edges that are part of the current execution path).
     */
    fun activeEdges(): List<FlowEdge> {
        return edges.values.filter { it.isActive }
    }

    /**
     * Check if the graph has a blocking error.
     */
    fun hasBlockingError(): Boolean {
        return nodes.values.any { it.status == NodeStatus.FAILED } ||
                execution?.status == ExecutionStatus.FAILED
    }

    /**
     * Get the primary blocking error message.
     */
    fun blockingErrorMessage(): String? {
        return nodes.values.find { it.status == NodeStatus.FAILED }?.errorMessage
            ?: execution?.errorMessage
    }
}
