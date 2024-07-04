package de.governikus.ausweisapp2.sdkwrapper.card.core.util

internal typealias WorkflowSimulator = de.governikus.ausweisapp2.sdkwrapper.card.core.Simulator
internal typealias WorkflowSimulatorFile = de.governikus.ausweisapp2.sdkwrapper.card.core.SimulatorFile
internal typealias WorkflowSimulatorKey = de.governikus.ausweisapp2.sdkwrapper.card.core.SimulatorKey

internal typealias CommandSimulator = de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.Simulator
internal typealias CommandSimulatorFile = de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.SimulatorFile
internal typealias CommandSimulatorKey = de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.SimulatorKey

internal fun workflowSimulatorToCommandSimulator(simulator: WorkflowSimulator?): CommandSimulator? {
    simulator ?: return null
    return CommandSimulator(
        simulator.files.map { workflowSimulatorFileToCommandSimulatorFile(it) },
        simulator.keys?.map { workflowSimulatorKeyToCommandSimulatorKey(it) },
    )
}

internal fun workflowSimulatorFileToCommandSimulatorFile(file: WorkflowSimulatorFile): CommandSimulatorFile {
    return CommandSimulatorFile(
        file.fileId,
        file.shortFileId,
        file.content,
    )
}

internal fun workflowSimulatorKeyToCommandSimulatorKey(key: WorkflowSimulatorKey): CommandSimulatorKey {
    return CommandSimulatorKey(
        key.id,
        key.content,
    )
}
