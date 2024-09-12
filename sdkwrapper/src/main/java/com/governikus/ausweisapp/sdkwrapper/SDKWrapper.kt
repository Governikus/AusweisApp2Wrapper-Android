/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.sdkwrapper

import com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowController
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.AA2SdkConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

object SDKWrapper : CoroutineScope by MainScope() {
    val workflowController = WorkflowController(AA2SdkConnection())
}
