/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper.card.core

/**
 * Authentication workflow callbacks.
 *
 * You need to register them with the [WorkflowController]
 *
 * @see WorkflowController.registerCallbacks
 */
interface WorkflowCallbacks {
    /**
     * [WorkflowController] has successfully been initialized.
     */
    fun onStarted()

    /**
     * An authentication has been started via [WorkflowController.startAuthentication].
     *
     * The next callback should be [onAccessRights] or [onAuthenticationCompleted] if the authentication immediately results in an error.
     */
    fun onAuthenticationStarted()

    /**
     * An authentication could not be started.
     * This is different from an authentication that was started but failed during the process.
     *
     * @param error Error message about why the authentication could not be started.
     */
    fun onAuthenticationStartFailed(error: String)

    /**
     * Indicates that the authentication workflow is completed.
     *
     * The [AuthResult] will contain a refresh url or in case of an error a communication error address.
     * You can check the state of the authentication, by looking for the [AuthResult.result] field.
     *
     * @param authResult Result of the authentication
     */
    fun onAuthenticationCompleted(authResult: AuthResult)

    /**
     * A pin change has been started via [WorkflowController.startChangePin].
     */
    fun onChangePinStarted()

    /**
     * Access rights requested in response to an authentication.
     *
     * This function will be called once the authentication is started by [WorkflowController.startAuthentication]
     * and the SDK got the certificate from the service.
     *
     * Accept ([WorkflowController.accept]) the rights to continue with the workflow or completely
     * abort the workflow with ([WorkflowController.cancel]).
     *
     * It is also possible to change the optional rights via [WorkflowController.setAccessRights].
     *
     * @param error Optional error message if the call to [WorkflowController.setAccessRights] failed.
     * @param accessRights Requested access rights.
     */
    fun onAccessRights(
        error: String?,
        accessRights: AccessRights?,
    )

    /**
     * Provides information about the used certificate.
     *
     * Response of a call to [WorkflowController.getCertificate].
     *
     * @param certificateDescription Requested certificate.
     */
    fun onCertificate(certificateDescription: CertificateDescription)

    /**
     * Indicates that the workflow now requires a card to continue.
     *
     * If your application receives this message it should show a hint to the user.
     * After the user inserted a card the workflow will automatically continue, unless the eID functionality is disabled.
     * In this case, the workflow will be paused until another card is inserted.
     * If the user already inserted a card this function will not be called at all.
     *
     * @param error Optional detailed error message if the previous call to [WorkflowController.setCard] failed.
     */
    fun onInsertCard(error: String?)

    /**
     * Called if the SDK is waiting on a certain condition to be met.
     *
     * After resolving the cause of the issue, the workflow has to be resumed by calling [WorkflowController.continueWorkflow].
     *
     * @param cause The cause for the waiting condition
     */
    fun onPause(cause: Cause)

    /**
     * A specific reader was recognized or has vanished. Also called as a response to [WorkflowController.getReader].
     *
     * @param reader Recognized or vanished reader, might be null if an unknown reader was requested  in [WorkflowController.getReader].
     */
    fun onReader(reader: Reader?)

    /**
     * Called as a reponse to [WorkflowController.getReaderList].
     *
     * @param readers Optional list of present readers (if any).
     */
    fun onReaderList(readers: List<Reader>?)

    /**
     * Indicates that a PIN is required to continue the workflow.
     *
     * A PIN is needed to unlock the id card, provide it with [WorkflowController.setPin].
     *
     * @param error Optional error message if the call to [WorkflowController.setPin] failed.
     * @param reader Reader the PIN is requested for
     */
    fun onEnterPin(
        error: String?,
        reader: Reader,
    )

    /**
     * Indicates that a new PIN is required to continue the workflow.
     *
     * A new PIN is needed fin response to a pin change, provide it with [WorkflowController.setNewPin].
     *
     * @param error Optional error message if the call to [WorkflowController.setNewPin] failed.
     * @param reader Reader the new PIN is requested for
     */
    fun onEnterNewPin(
        error: String?,
        reader: Reader,
    )

    /**
     * Indicates that a PUK is required to continue the workflow.
     *
     * A PUK is needed to unlock the id card, provide it with [WorkflowController.setPuk].
     *
     * @param error Optional error message if the call to [WorkflowController.setPuk] failed.
     * @param reader Reader the PUK is requested for
     */
    fun onEnterPuk(
        error: String?,
        reader: Reader,
    )

    /**
     * Indicates that a CAN is required to continue workflow.
     *
     * A CAN is needed to unlock the id card, provide it with [WorkflowController.setCan].
     *
     * @param error Optional error message if the call to [WorkflowController.setCan] failed.
     * @param reader Reader the CAN is requested for
     */
    fun onEnterCan(
        error: String?,
        reader: Reader,
    )

    /**
     * Indicates that the pin change workflow is completed.
     *
     * @param changePinResult Result of the pin change
     */
    fun onChangePinCompleted(changePinResult: ChangePinResult)

    /**
     * Indicates that an error has occurred.
     *
     * This might be called if there was an error in the workflow.
     *
     * @param error Error
     */
    fun onWrapperError(error: WrapperError)

    /**
     * Provides information about the current workflow and state. This callback indicates if a
     * workflow is in progress or the workflow is paused. This can occur if the AusweisApp2 needs
     * additional data like ACCESS_RIGHTS or INSERT_CARD.
     *
     * @param workflowProgress Holds information about the current workflow progress.
     */
    fun onStatus(workflowProgress: WorkflowProgress)

    /**
     * Provides information about the AusweisApp2 that is used in the SDK Wrapper.
     *
     * Response to a call to [WorkflowController.getInfo].
     *
     * @param versionInfo Holds information about the currently utilized AusweisApp2.
     */
    fun onInfo(versionInfo: VersionInfo)

    /**
     * Called if an error within the AusweisApp2 SDK occurred. Please report this as it indicates a bug.
     *
     * @param error Information about the error.
     */
    fun onInternalError(error: String)

    /**
     * Called if the sent command is not allowed within the current workflow.
     *
     * @param error Error message which SDK command failed.
     */
    fun onBadState(error: String)
}
