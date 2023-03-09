package de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol

internal data class Simulator(
    val files: List<SimulatorFile>
)

internal data class SimulatorFile(
    val fileId: String,
    val shortFileId: String,
    val content: String
)
