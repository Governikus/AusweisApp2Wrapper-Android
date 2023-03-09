/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2

import android.net.Uri
import de.governikus.ausweisapp2.sdkwrapper.card.core.AccessRight
import de.governikus.ausweisapp2.sdkwrapper.card.core.AccessRights
import de.governikus.ausweisapp2.sdkwrapper.card.core.ApiLevel
import de.governikus.ausweisapp2.sdkwrapper.card.core.AuthResult
import de.governikus.ausweisapp2.sdkwrapper.card.core.AuthResultData
import de.governikus.ausweisapp2.sdkwrapper.card.core.AuxiliaryData
import de.governikus.ausweisapp2.sdkwrapper.card.core.Card
import de.governikus.ausweisapp2.sdkwrapper.card.core.CertificateDescription
import de.governikus.ausweisapp2.sdkwrapper.card.core.CertificateValidity
import de.governikus.ausweisapp2.sdkwrapper.card.core.Reader
import de.governikus.ausweisapp2.sdkwrapper.card.core.VersionInfo
import de.governikus.ausweisapp2.sdkwrapper.card.core.WorkflowProgress
import de.governikus.ausweisapp2.sdkwrapper.card.core.WorkflowProgressType
import de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.Message
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)

internal fun Message.getCertificateDescription(): CertificateDescription? {
    val description = description ?: return null
    val validity = validity ?: return null
    val issueDate = dateFormat.parse(validity.effectiveDate) ?: return null
    val expirationDate = dateFormat.parse(validity.expirationDate) ?: return null

    val issuerUrl = if (description.issuerUrl.isNotBlank()) Uri.parse(description.issuerUrl) else null
    val subjectUrl = if (description.subjectUrl.isNotBlank()) Uri.parse(description.subjectUrl) else null

    return CertificateDescription(
        description.issuerName,
        issuerUrl,
        description.purpose,
        description.subjectName,
        subjectUrl,
        description.termsOfUsage,
        CertificateValidity(
            issueDate,
            expirationDate
        )
    )
}

internal fun Message.getCard(): Card? {
    val card = card ?: reader?.card ?: return null

    return Card(
        card.deactivated,
        card.inoperative,
        card.retryCounter
    )
}

internal fun Message.getApiLevel(): ApiLevel? {
    val current = current ?: return null

    return ApiLevel(
        available,
        current
    )
}

internal fun Message.getReaderFromRoot(): Reader? {
    val name = name ?: return null
    val insertable = insertable ?: return null
    val attached = attached ?: return null
    val keypad = keypad ?: return null

    return Reader(
        name,
        insertable,
        attached,
        keypad,
        getCard()
    )
}

internal fun Message.getReaderFromReaderMember(): Reader? {
    val reader = reader ?: return null
    val name = reader.name
    val insertable = reader.insertable
    val keypad = reader.keypad
    val attached = reader.attached

    return Reader(
        name,
        insertable,
        attached,
        keypad,
        getCard()
    )
}

internal fun Message.getReaderList(): List<Reader>? {
    val readers = readers ?: return null

    return readers.map {
        val card = if (it.card == null) {
            null
        } else {
            Card(
                it.card.deactivated,
                it.card.inoperative,
                it.card.retryCounter
            )
        }
        Reader(
            it.name,
            it.insertable,
            it.attached,
            it.keypad,
            card
        )
    }
}

internal fun Message.getVersionInfo(): VersionInfo? {
    val info = versionInfo ?: return null

    return VersionInfo(
        info.name,
        info.implementationTitle,
        info.implementationVendor,
        info.implementationVersion,
        info.specificationTitle,
        info.specificationVendor,
        info.specificationVersion
    )
}

internal fun Message.getAccessRights(): AccessRights? {
    val chat = chat ?: return null

    val auxiliaryData = aux?.run {
        AuxiliaryData(
            if (ageVerificationDate != null) dateFormat.parse(ageVerificationDate) else null,
            requiredAge?.toInt(),
            if (validityDate != null) dateFormat.parse(validityDate) else null,
            communityId
        )
    }

    val requiredRights = chat.required.mapNotNull { AccessRight.fromRawName(it) }
    val optionalRights = chat.optional.mapNotNull { AccessRight.fromRawName(it) }
    val effectiveRights = chat.effective.mapNotNull { AccessRight.fromRawName(it) }

    return AccessRights(
        requiredRights,
        optionalRights,
        effectiveRights,
        transactionInfo,
        auxiliaryData
    )
}

internal fun Message.getAuthResult(): AuthResult? {
    val uri = if (url != null) Uri.parse(url) else null
    val resultData = getAuthResultData()

    if (resultData != null || uri != null) {
        return AuthResult(uri, resultData)
    }

    return null
}

internal fun Message.getAuthResultData(): AuthResultData? {
    val result = result ?: return null
    if (result.major == null) return null

    return AuthResultData(
        result.major,
        result.minor,
        result.language,
        result.description,
        result.message,
        result.reason
    )
}

internal fun Message.getWorkflowProgress(): WorkflowProgress {
    val workflowType = WorkflowProgressType.fromRawName(workflow)
    return WorkflowProgress(workflowType, progress, state)
}
