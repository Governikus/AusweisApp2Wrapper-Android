package com.governikus.ausweisapp.sdkwrapper.card.core.util

internal typealias WorkflowSimulator = com.governikus.ausweisapp.sdkwrapper.card.core.Simulator
internal typealias WorkflowSimulatorFile = com.governikus.ausweisapp.sdkwrapper.card.core.SimulatorFile
internal typealias WorkflowSimulatorKey = com.governikus.ausweisapp.sdkwrapper.card.core.SimulatorKey

internal typealias CommandSimulator = com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Simulator
internal typealias CommandSimulatorFile = com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SimulatorFile
internal typealias CommandSimulatorKey = com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SimulatorKey

internal fun workflowSimulatorToCommandSimulator(simulator: WorkflowSimulator?): CommandSimulator? {
    simulator ?: return null
    return CommandSimulator(
        simulator.files.map { workflowSimulatorFileToCommandSimulatorFile(it) },
        simulator.keys?.map { workflowSimulatorKeyToCommandSimulatorKey(it) },
    )
}

internal fun workflowSimulatorFileToCommandSimulatorFile(file: WorkflowSimulatorFile): CommandSimulatorFile =
    CommandSimulatorFile(
        file.fileId,
        file.shortFileId,
        file.content,
    )

internal fun workflowSimulatorKeyToCommandSimulatorKey(key: WorkflowSimulatorKey): CommandSimulatorKey =
    CommandSimulatorKey(
        key.id,
        key.content,
    )
