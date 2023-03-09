package de.governikus.ausweisapp2.sdkwrapper.card.core.util

internal typealias WorkflowSimulator = de.governikus.ausweisapp2.sdkwrapper.card.core.Simulator
internal typealias WorkflowSimulatorFile = de.governikus.ausweisapp2.sdkwrapper.card.core.SimulatorFile

internal typealias CommandSimulator = de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.Simulator
internal typealias CommandSimulatorFile = de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.SimulatorFile

internal fun workflowSimulatorToCommandSimulator(simulator: WorkflowSimulator?): CommandSimulator? {
    simulator ?: return null
    return CommandSimulator(
        simulator.files.map { workflowSimulatorFileToCommandSimulatorFile(it) }
    )
}

internal fun workflowSimulatorFileToCommandSimulatorFile(file: WorkflowSimulatorFile): CommandSimulatorFile {
    return CommandSimulatorFile(
        file.fileId,
        file.shortFileId,
        file.content
    )
}
