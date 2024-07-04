/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol

import com.google.gson.annotations.SerializedName

internal object Messages {
    const val MSG_ACCESS_RIGHTS = "ACCESS_RIGHTS"
    const val MSG_AUTH = "AUTH"
    const val MSG_BAD_STATE = "BAD_STATE"
    const val MSG_CERTIFICATE = "CERTIFICATE"
    const val MSG_CHANGE_PIN = "CHANGE_PIN"
    const val MSG_ENTER_CAN = "ENTER_CAN"
    const val MSG_ENTER_NEW_PIN = "ENTER_NEW_PIN"
    const val MSG_INFO = "INFO"
    const val MSG_ENTER_PIN = "ENTER_PIN"
    const val MSG_ENTER_PUK = "ENTER_PUK"
    const val MSG_INSERT_CARD = "INSERT_CARD"
    const val MSG_INTERNAL_ERROR = "INTERNAL_ERROR"
    const val MSG_INVALID = "INVALID"
    const val MSG_PAUSE = "PAUSE"
    const val MSG_READER = "READER"
    const val MSG_READER_LIST = "READER_LIST"
    const val MSG_STATUS = "STATUS"
    const val MSG_UNKNOWN_COMMAND = "UNKNOWN_COMMAND"
}

internal data class Message(
    val attached: Boolean?,
    val aux: Aux?,
    val card: Card?,
    val cause: String?,
    val chat: Chat?,
    val description: Description?,
    val error: String?,
    val insertable: Boolean?,
    val keypad: Boolean?,
    val msg: String?,
    val name: String?,
    val progress: Int?,
    val reader: Reader?,
    val readers: List<Reader>?,
    val reason: String?,
    val result: Result?,
    val state: String?,
    val success: Boolean?,
    val transactionInfo: String?,
    val url: String?,
    val validity: Validity?,
    @SerializedName("VersionInfo")
    val versionInfo: VersionInfo?,
    val workflow: String?,
)

internal data class Description(
    val issuerName: String,
    val issuerUrl: String,
    val purpose: String,
    val subjectName: String,
    val subjectUrl: String,
    val termsOfUsage: String,
)

internal data class Validity(
    val effectiveDate: String,
    val expirationDate: String,
)

internal data class Chat(
    val effective: List<String>,
    val optional: List<String>,
    val required: List<String>,
)

internal data class Aux(
    val ageVerificationDate: String?,
    val requiredAge: String?,
    val validityDate: String?,
    val communityId: String?,
)

internal data class Card(
    val inoperative: Boolean?,
    val deactivated: Boolean?,
    val retryCounter: Int?,
)

internal data class Reader(
    val name: String,
    val insertable: Boolean,
    val attached: Boolean,
    val keypad: Boolean,
    val card: Card?,
)

internal data class Result(
    val major: String?,
    val minor: String?,
    val language: String?,
    val description: String?,
    val message: String?,
    val reason: String?,
)

internal data class VersionInfo(
    @SerializedName("Name")
    val name: String,
    @SerializedName("Implementation-Title")
    val implementationTitle: String,
    @SerializedName("Implementation-Vendor")
    val implementationVendor: String,
    @SerializedName("Implementation-Version")
    val implementationVersion: String,
    @SerializedName("Specification-Title")
    val specificationTitle: String,
    @SerializedName("Specification-Vendor")
    val specificationVendor: String,
    @SerializedName("Specification-Version")
    val specificationVersion: String,
)
