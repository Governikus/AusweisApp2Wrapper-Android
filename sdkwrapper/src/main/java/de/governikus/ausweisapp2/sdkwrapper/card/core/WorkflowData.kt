/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper.card.core

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Detailed description of the certificate.
 *
 * @property issuerName Name of the certificate issuer.
 * @property issuerUrl URL of the certificate issuer.
 * @property subjectName Name of the certificate subject.
 * @property subjectUrl URL of the certificate subject.
 * @property termsOfUsage Raw certificate information about the terms of usage.
 * @property purpose Parsed purpose of the terms of usage.
 * @property validity Certificate validity
 */
@Parcelize
data class CertificateDescription(
    val issuerName: String,
    val issuerUrl: Uri?,
    val purpose: String,
    val subjectName: String,
    val subjectUrl: Uri?,
    val termsOfUsage: String,
    val validity: CertificateValidity,
) : Parcelable

/**
 * Validity dates of the certificate in UTC.
 *
 * @property effectiveDate Certificate is valid since this date.
 * @property expirationDate Certificate is invalid after this date.
 */
@Parcelize
data class CertificateValidity(
    val effectiveDate: Date,
    val expirationDate: Date,
) : Parcelable

/**
 * Access rights requested by the provider.
 *
 * @property requiredRights These rights are mandatory and cannot be disabled.
 * @property optionalRights These rights are optional and can be enabled or disabled
 * @property effectiveRights Indicates the enabled access rights of optional and required.
 * @property transactionInfo Optional transaction information.
 * @property auxiliaryData Optional auxiliary data of the provider.
 */
@Parcelize
data class AccessRights(
    val requiredRights: List<AccessRight>,
    val optionalRights: List<AccessRight>,
    val effectiveRights: List<AccessRight>,
    val transactionInfo: String?,
    val auxiliaryData: AuxiliaryData?,
) : Parcelable

/**
 * Auxiliary data of the provider.
 *
 * @property ageVerificationDate Optional required date of birth for AgeVerification.
 * @property requiredAge Optional required age for AgeVerification. It is calculated by the SDK on the basis of ageVerificationDate and current date.
 * @property validityDate Optional validity date.
 * @property communityId Optional id of community.
 */
@Parcelize
data class AuxiliaryData(
    val ageVerificationDate: Date?,
    val requiredAge: Int?,
    val validityDate: Date?,
    val communityId: String?,
) : Parcelable

/**
 * Provides information about inserted card.
 *
 * An unknown card (without eID function) is represented by all properties set to null.
 *
 * @property inoperative True if PUK is inoperative and cannot unblock PIN, otherwise false. This can be recognized if user enters a correct PUK only. It is not possible to read this data before a user tries to unblock the PIN.
 * @property deactivated True if eID functionality is deactivated, otherwise false.
 * @property pinRetryCounter Count of possible retries for the PIN. If you enter a PIN it will be decreased if PIN was incorrect.
 */
@Parcelize
data class Card(
    val deactivated: Boolean?,
    val inoperative: Boolean?,
    val pinRetryCounter: Int?,
) : Parcelable

/**
 * Convenience method to check if an unknown card (without eID function) was detected.
 */
fun Card.isUnknown(): Boolean = inoperative == null && deactivated == null && pinRetryCounter == null

/**
 * List of possible causes in [WorkflowCallbacks.onPause]
 */
enum class Cause(val rawName: String) {
    BadCardPosition("BadCardPosition"), // Denotes an unstable or lost card connection.
    ;

    companion object {
        fun fromRawName(name: String?): Cause? = values().firstOrNull { it.rawName == name }
    }
}

/**
 * Optional definition of files and keys for the Simulator reader.
 *
 * @property files List of Filesystem definitions. See [SimulatorFile].
 * @property keys List of SimulatorKey definitions. See [SimulatorKey].
 */
@Parcelize
data class Simulator(
    val files: List<SimulatorFile>,
    val keys: List<SimulatorKey>?,
) : Parcelable

/**
 * Filesystem for Simulator reader
 *
 * The content of the filesystem can be provided as a JSON array of objects. The fileId and shortFileId
 * are specified on the corresponding technical guideline of the BSI and ISO. The content is an
 * ASN.1 structure in DER encoding.
 *
 * All fields are hex encoded.
 *
 * @property fileId The fileId and shortFileId are specified on the corresponding technical guideline of the BSI and ISO.
 * @property shortFileId The fileId just shorter.
 * @property content The content is an ASN.1 structure in DER encoding.
 */
@Parcelize
data class SimulatorFile(
    val fileId: String,
    val shortFileId: String,
    val content: String,
) : Parcelable

/**
 * Keys for Simulator reader
 *
 * The keys are used to check the blacklist and calculate the pseudonym for the service provider.
 */
@Parcelize
data class SimulatorKey(
    val id: Int,
    val content: String,
) : Parcelable

/**
 * Final result of an authentication.
 *
 * @property url Refresh url or an optional communication error address.
 * @property result Contains information about the result of the authentication. See [AuthResultData].
 */
@Parcelize
data class AuthResult(
    val url: Uri?,
    val result: AuthResultData?,
) : Parcelable

/**
 * Final result of a PIN change.
 *
 * @property success True if a the PIN has been successfully set, else false.
 * @property reason Unique error code if the PIN change failed.
 */

@Parcelize
data class ChangePinResult(
    val success: Boolean,
    val reason: String?,
) : Parcelable

/**
 * Information about an authentication
 *
 * @property major Major error code.
 * @property minor Minor error code.
 * @property language Language of description and message. Language “en” is supported only at the moment.
 * @property description Description of the error message.
 * @property message The error message.
 * @property reason Unique error code.
 */
@Parcelize
data class AuthResultData(
    val major: String,
    val minor: String?,
    val language: String?,
    val description: String?,
    val message: String?,
    val reason: String?,
) : Parcelable

/**
 * Provides information about a reader.
 *
 * @property name Identifier of card reader.
 * @property insertable Indicates whether a card can be inserted via setCard().
 * @property attached Indicates whether a card reader is connected or disconnected.
 * @property keypad Indicates whether a card reader has a keypad.
 * @property card Provides information about inserted card, otherwise null.
 */
@Parcelize
data class Reader(
    val name: String,
    val insertable: Boolean,
    val attached: Boolean,
    val keypad: Boolean,
    val card: Card?,
) : Parcelable

/**
 * Provides information about the underlying AusweisApp2.
 *
 * @property name Application name.
 * @property implementationTitle Title of implementation.
 * @property implementationVendor Vendor of implementation.
 * @property implementationVersion Version of implementation.
 * @property specificationTitle Títle of specification.
 * @property specificationVendor Vendor of specification.
 * @property specificationVersion Version of specification.
 *
 */
@Parcelize
data class VersionInfo(
    val name: String,
    val implementationTitle: String,
    val implementationVendor: String,
    val implementationVersion: String,
    val specificationTitle: String,
    val specificationVendor: String,
    val specificationVersion: String,
) : Parcelable

/**
 * Provides information about an error.
 *
 * @property msg Message type in which the error occurred.
 * @property error Error message.
 */
@Parcelize
data class WrapperError(
    val msg: String,
    val error: String,
) : Parcelable

/**
 * Provides information about the workflow status
 *
 * @param workflow Type of the current workflow. If there is no workflow in progress this will
 * be null.
 * @param progress Percentage of workflow progress. If there is no workflow in progress this
 * will be null.
 * @param state Name of the current state if paused. If there is no workflow in progress or the
 * workflow is not paused this will be null.
 */
@Parcelize
data class WorkflowProgress(
    val workflow: WorkflowProgressType?,
    val progress: Int?,
    val state: String?,
) : Parcelable

/**
 * List of all available access rights a provider might request.
 */
enum class AccessRight(val rawName: String) {
    ADDRESS("Address"),
    BIRTH_NAME("BirthName"),
    FAMILY_NAME("FamilyName"),
    GIVEN_NAMES("GivenNames"),
    PLACE_OF_BIRTH("PlaceOfBirth"),
    DATE_OF_BIRTH("DateOfBirth"),
    DOCTORAL_DEGREE("DoctoralDegree"),
    ARTISTIC_NAME("ArtisticName"),
    PSEUDONYM("Pseudonym"),
    VALID_UNTIL("ValidUntil"),
    NATIONALITY("Nationality"),
    ISSUING_COUNTRY("IssuingCountry"),
    DOCUMENT_TYPE("DocumentType"),
    RESIDENCE_PERMIT_I("ResidencePermitI"),
    RESIDENCE_PERMIT_II("ResidencePermitII"),
    COMMUNITY_ID("CommunityID"),
    ADDRESS_VERIFICATION("AddressVerification"),
    AGE_VERIFICATION("AgeVerification"),
    WRITE_ADDRESS("WriteAddress"),
    WRITE_COMMUNITY_ID("WriteCommunityID"),
    WRITE_RESIDENCE_PERMIT_I("WriteResidencePermitI"),
    WRITE_RESIDENCE_PERMIT_II("WriteResidencePermitII"),
    CAN_ALLOWED("CanAllowed"),
    PIN_MANAGEMENT("PinManagement"),
    ;

    companion object {
        fun fromRawName(rawName: String) = values().firstOrNull { it.rawName == rawName }
    }
}

/**
 * List of all types of WorkflowProgress
 */
enum class WorkflowProgressType(val rawName: String) {
    AUTHENTICATION("AUTH"),
    CHANGE_PIN("CHANGE_PIN"),
    ;

    companion object {
        fun fromRawName(rawName: String?) = values().firstOrNull { it.rawName == rawName }
    }
}
