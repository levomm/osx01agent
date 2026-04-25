package com.cemnect.android.domain.model

/**
 * Represents a node in the task execution flow graph.
 */
data class FlowNode(
    val id: String,
    val name: String,
    val type: FlowNodeType,
    val status: NodeStatus,
    val description: String = "",
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val errorMessage: String? = null
)

/**
 * Types of nodes that can appear in the flow graph.
 */
enum class FlowNodeType {
    START,
    ACTION,
    DECISION,
    WAIT,
    PARALLEL_BRANCH,
    MERGE,
    END
}

/**
 * Status of a node in the flow graph.
 */
enum class NodeStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    BLOCKED,
    SKIPPED
}

/**
 * Represents a connection/edge between two nodes in the flow graph.
 */
data class FlowEdge(
    val id: String,
    val sourceNodeId: String,
    val targetNodeId: String,
    val label: String = "",
    val isActive: Boolean = false
)

/**
 * Represents a complete task definition with its flow graph.
 */
data class Task(
    val id: String,
    val name: String,
    val description: String,
    val flowNodes: List<FlowNode>,
    val flowEdges: List<FlowEdge>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Represents the current execution state of a task.
 */
data class TaskExecution(
    val taskId: String,
    val status: ExecutionStatus,
    val currentNodeId: String?,
    val progress: Float, // 0.0 to 1.0
    val startTime: Long,
    val endTime: Long? = null,
    val errorMessage: String? = null
)

/**
 * Overall status of a task execution.
 */
enum class ExecutionStatus {
    NOT_STARTED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
