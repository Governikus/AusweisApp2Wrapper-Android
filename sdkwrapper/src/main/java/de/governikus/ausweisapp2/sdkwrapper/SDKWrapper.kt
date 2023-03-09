/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper

import de.governikus.ausweisapp2.sdkwrapper.card.core.WorkflowController
import de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.AA2SdkConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

object SDKWrapper : CoroutineScope by MainScope() {
    val workflowController = WorkflowController(AA2SdkConnection())
}
