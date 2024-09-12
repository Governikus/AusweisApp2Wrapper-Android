package com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol

internal data class Simulator(
    val files: List<SimulatorFile>,
    val keys: List<SimulatorKey>?,
)

internal data class SimulatorFile(
    val fileId: String,
    val shortFileId: String,
    val content: String,
)

internal data class SimulatorKey(
    val id: Int,
    val content: String,
)
