/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core

import android.content.Context
import android.net.Uri
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import com.governikus.ausweisapp.sdkwrapper.SDKWrapper
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.getAccessRights
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.getAuthResult
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.getCertificateDescription
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.getReaderFromReaderMember
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.getReaderFromRoot
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.getReaderList
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.getVersionInfo
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.getWorkflowProgress
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Accept
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Cancel
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Command
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.ContinueWorkflow
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetAccessRights
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetCertificate
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetInfo
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetReader
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetReaderList
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetStatus
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Message
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_ACCESS_RIGHTS
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_AUTH
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_BAD_STATE
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_CERTIFICATE
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_CHANGE_PIN
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_ENTER_CAN
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_ENTER_NEW_PIN
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_ENTER_PIN
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_ENTER_PUK
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_INFO
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_INSERT_CARD
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_INTERNAL_ERROR
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_INVALID
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_PAUSE
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_READER
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_READER_LIST
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_STATUS
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Messages.MSG_UNKNOWN_COMMAND
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.RunAuth
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.RunChangePin
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetAccessRights
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetCan
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetCard
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetNewPin
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetPin
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetPuk
import com.governikus.ausweisapp.sdkwrapper.card.core.util.workflowSimulatorToCommandSimulator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * [WorkflowController] is used to control the authentication and pin change workflow
 */
class WorkflowController internal constructor(
    private val sdkConnection: SdkConnection,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    internal interface SdkConnection {
        val isConnected: Boolean

        fun bind(
            context: Context,
            onConnected: (() -> Unit)? = null,
            onConnectionFailed: (() -> Unit)? = null,
            onMessageReceived: ((message: Message) -> Unit)? = null,
        )

        fun unbind()

        fun updateNfcTag(tag: Tag): Boolean

        fun <T : Command> send(command: T): Boolean
    }

    private val workflowCallbacks = ArrayList<WorkflowCallbacks>()
    private var isStarting: Boolean = false

    /**
     * Indicates that the [WorkflowController] is ready to be used.
     * When the [WorkflowController] is not in started state, other api calls will fail.
     */
    val isStarted: Boolean
        get() = sdkConnection.isConnected

    /**
     * Initialize the [WorkflowController].
     *
     * Before it is possible to use the [WorkflowController] it needs to be initialized.
     * Make sure to call this function and wait for the [WorkflowCallbacks.onStarted] callback before using it.
     *
     * @param context Context
     */
    fun start(context: Context) {
        if (isStarting) {
            Log.d(TAG, "WorkflowController is already starting")
            return
        }
        if (isStarted) {
            Log.d(TAG, "WorkflowController already started")
            return
        }

        isStarting = true

        sdkConnection.bind(
            context,
            onConnected = {
                isStarting = false
                callback { onStarted() }
            },
            onConnectionFailed = {
                isStarting = false
                val error = WrapperError("WorkflowController::start", "Connection failed")
                callback { onWrapperError(error) }
            },
            onMessageReceived = { messageJson ->
                handleMessage(messageJson)
            },
        )
    }

    /**
     * Stop the [WorkflowController].
     *
     * When you no longer need the [WorkflowController] make sure to stop it to free up some
     * resources.
     */
    fun stop() {
        if (!isStarted && !isStarting) {
            Log.d(TAG, "WorkflowController not started")
            return
        }
        isStarting = false
        sdkConnection.unbind()
    }

    /**
     * Register callbacks with controller.
     *
     * @param callbacks Callbacks to register.
     */
    fun registerCallbacks(callbacks: WorkflowCallbacks) {
        workflowCallbacks.add(callbacks)
    }

    /**
     * Unregister callback from controller.
     *
     * @param callbacks Callbacks to unregister.
     */
    fun unregisterCallbacks(callbacks: WorkflowCallbacks) {
        workflowCallbacks.remove(callbacks)
    }

    /**
     * Starts an authentication workflow.
     *
     * The [WorkflowController] will call [WorkflowCallbacks.onAuthenticationStarted],
     * when the authentication is started. If there was an error starting the authentication
     * [WorkflowCallbacks.onAuthenticationStartFailed].
     *
     * After calling this method, the expected minimal workflow is:
     * [WorkflowCallbacks.onAuthenticationStarted] is called.
     * When [WorkflowCallbacks.onAccessRights] is called, accept it via [accept].
     * [WorkflowCallbacks.onInsertCard] is called, when the user has not yet placed the phone on the card.
     * When [WorkflowCallbacks.onEnterPin] is called, provide the pin via [setPin].
     * When the authentication workflow is finished [WorkflowCallbacks.onAuthenticationCompleted] is called.
     *
     * This command is allowed only if the SDK has no running workflow.
     * Otherwise you will get a callback to [WorkflowCallbacks.onBadState].
     *
     * @param tcTokenUrl URL to the TcToken.
     *
     * @param developerMode Enable "Developer Mode" for test cards and disable some security
     * checks according to BSI TR-03124-1.
     *
     * @param status True to enable automatic STATUS messages, which are delivered by
     * callbacks to [WorkflowCallbacks.onStatus].
     */
    fun startAuthentication(
        tcTokenUrl: Uri,
        developerMode: Boolean = false,
        status: Boolean = true,
    ) {
        send(RunAuth(tcTokenUrl.toString(), developerMode, status))
    }

    /**
     * Start a pin change workflow.
     *
     * The [WorkflowController] will call [WorkflowCallbacks.onChangePinStarted],
     * when the pin change is started.
     *
     * After calling this method, the expected minimal workflow is:
     * [WorkflowCallbacks.onChangePinStarted] is called.
     * [WorkflowCallbacks.onInsertCard] is called, when the user has not yet placed the card on the reader.
     * When [WorkflowCallbacks.onEnterPin] is called, provide the pin via [setPin].
     * When [WorkflowCallbacks.onEnterNewPin] is called, provide the new pin via [setNewPin].
     * When the pin workflow is finished, [WorkflowCallbacks.onChangePinCompleted] is called.
     *
     * This command is allowed only if the SDK has no running workflow.
     * Otherwise you will get a callback to [WorkflowCallbacks.onBadState].
     *
     * @param status True to enable automatic STATUS messages, which are delivered by
     * callbacks to [WorkflowCallbacks.onStatus].
     */
    fun startChangePin(status: Boolean = true) {
        send(RunChangePin(status))
    }

    /**
     * Set optional access rights
     *
     * If the SDK asks for specific access rights in [WorkflowCallbacks.onAccessRights],
     * you can modify the requested optional rights by setting a list of accepted optional rights here.
     * When the command is successful you get a callback to [WorkflowCallbacks.onAccessRights]
     * with the updated access rights.
     *
     * List of possible access rights are listed in [AccessRight]
     *
     * This command is allowed only if the SDK asked for a pin via [WorkflowCallbacks.onAccessRights].
     * Otherwise you will get a callback to [WorkflowCallbacks.onBadState].
     *
     * @param accessRights List of enabled optional access rights. If the list is empty all optional access rights are disabled.
     */
    fun setAccessRights(accessRights: List<AccessRight>) {
        send(SetAccessRights(accessRights.map { it.rawName }))
    }

    /**
     * Returns information about the requested access rights.
     * This command is allowed only if the SDK Wrapper called [WorkflowCallbacks.onAccessRights] beforehand.
     */
    fun getAccessRights() {
        send(GetAccessRights())
    }

    /**
     * Provides information about the utilized AusweisApp2.
     *
     * The SDK Wrapper will call [WorkflowCallbacks.onInfo] as an answer.
     */
    fun getInfo() {
        send(GetInfo())
    }

    /**
     * Dummy match for SDK Wrapper iOS method, does not actually do anything at the moment.
     */
    fun interrupt() {
        // Will not be called on Android yet.
    }

    /**
     * Set PIN of inserted card.
     *
     * If the SDK calls [WorkflowCallbacks.onEnterPin] you need to call this function to unblock the card with the PIN.
     *
     * If your application provides an invalid PIN the SDK will call [WorkflowCallbacks.onEnterPin]
     * again with a decreased retryCounter.
     *
     * If the value of retryCounter is 1 the SDK will initially call [WorkflowCallbacks.onEnterCan].
     * Once your application provides a correct CAN the SDK will call [WorkflowCallbacks.onEnterPin]
     * again with a retryCounter of 1.
     * If the value of retryCounter is 0 the SDK will initially call [WorkflowCallbacks.onEnterPuk].
     * Once your application provides a correct PUK the SDK will call [WorkflowCallbacks.onEnterPin]
     * again with a retryCounter of 3.
     *
     * This command is allowed only if the SDK asked for a pin via [WorkflowCallbacks.onEnterPin].
     * Otherwise you will get a callback to [WorkflowCallbacks.onBadState].
     *
     * @param pin The personal identification number (PIN) of the card. Must contain 5 (Transport PIN) or 6 digits.
     */
    fun setPin(pin: String?) {
        send(SetPin(pin))
    }

    /**
     * Set new PIN for inserted card.
     *
     * If the SDK calls [WorkflowCallbacks.onEnterNewPin] you need to call this function to provide a new pin.
     *
     * This command is allowed only if the SDK asked for a new pin via [WorkflowCallbacks.onEnterNewPin].
     * Otherwise you will get a callback to [WorkflowCallbacks.onBadState].
     *
     * @param newPin The new personal identification number (PIN) of the card. Must only contain 6 digits.
     * Must be null if the current reader has a keypad. See [Reader].
     */
    fun setNewPin(newPin: String?) {
        send(SetNewPin(newPin))
    }

    /**
     * Set PUK of inserted card.
     *
     * If the SDK calls [WorkflowCallbacks.onEnterPuk] you need to call this function to unblock [setPin].
     *
     * The workflow will automatically continue if the PUK was correct and the SDK will call [WorkflowCallbacks.onEnterPin].
     * If the correct PUK is entered the retryCounter will be set to 3.
     *
     * If your application provides an invalid PUK the SDK will call [WorkflowCallbacks.onEnterPuk] again.
     *
     * If the SDK calls [WorkflowCallbacks.onEnterPuk] with [Card.inoperative] set true it is not possible to unblock the PIN.
     * You will have to show a message to the user that the card is inoperative and the user should
     * contact the authority responsible for issuing the identification card to unblock the PIN.
     *
     * This command is allowed only if the SDK asked for a puk via [WorkflowCallbacks.onEnterPuk].
     * Otherwise you will get a callback to [WorkflowCallbacks.onBadState].
     *
     * @param puk The personal unblocking key (PUK) of the card. Must only contain 10 digits.
     * Must be null if the current reader has a keypad. See [Reader].
     */
    fun setPuk(puk: String?) {
        send(SetPuk(puk))
    }

    /**
     * Set CAN of inserted card.
     *
     * If the SDK calls [WorkflowCallbacks.onEnterCan] you need to call this function to unblock the last retry of [setPin].
     *
     * The CAN is required to enable the last attempt of PIN input if the retryCounter is 1.
     * The workflow continues automatically with the correct CAN and the SDK will call [WorkflowCallbacks.onEnterPin].
     * Despite the correct CAN being entered, the retryCounter remains at 1.
     * The CAN is also required, if the authentication terminal has an approved “CAN allowed right”.
     * This allows the workflow to continue without an additional PIN.
     *
     * If your application provides an invalid CAN the SDK will call [WorkflowCallbacks.onEnterCan] again.
     *
     * This command is allowed only if the SDK asked for a puk via [WorkflowCallbacks.onEnterCan].
     * Otherwise you will get a callback to [WorkflowCallbacks.onBadState].
     *
     * @param can The card access number (CAN) of the card. Must only contain 6 digits.
     * Must be null if the current reader has a keypad. See [Reader].
     */
    fun setCan(can: String?) {
        send(SetCan(can))
    }

    /**
     * Insert “virtual” card.
     *
     * @param name Name of [Reader] with a [Card] that shall be used.
     * @param simulator Specific data for [Simulator]. (optional) files: Content of card Filesystem.
     */
    fun setCard(
        name: String,
        simulator: Simulator?,
    ) {
        send(SetCard(name, workflowSimulatorToCommandSimulator(simulator)))
    }

    /**
     * Accept the current state.
     *
     * If the SDK calls [WorkflowCallbacks.onAccessRights] the user needs to accept or deny them.
     * The workflow is paused until your application sends this command to accept the requested information.
     * If the user does not accept the requested information your application needs to call [cancel] to abort the whole workflow.
     *
     * This command is allowed only if the SDK asked for access rights via [WorkflowCallbacks.onAccessRights].
     * Otherwise you will get a callback to [WorkflowCallbacks.onBadState].
     *
     *  Note: This accepts the requested access rights as well as the provider's certificate since it is not possible to accept
     *  one without the other.
     */
    fun accept() {
        send(Accept())
    }

    /**
     * Cancel the running workflow.
     *
     * If your application sends this command the SDK will cancel the workflow.
     * You can send this command in any state of a running workflow to abort it.
     */
    fun cancel() {
        send(Cancel())
    }

    /**
     * Resumes the workflow after a callback to [WorkflowCallbacks.onPause].
     */
    fun continueWorkflow() {
        send(ContinueWorkflow())
    }

    /**
     * Request the certificate of current authentication.
     *
     * The SDK will call [WorkflowCallbacks.onCertificate] as an answer.
     */
    fun getCertificate() {
        send(GetCertificate())
    }

    /**
     * Request information about the current workflow and state of SDK.
     * The SDK will call [WorkflowCallbacks.onStatus] as an answer.
     */
    fun getStatus() {
        send(GetStatus())
    }

    /**
     * Returns information about the requested reader.
     *
     * If you explicitly want to ask for information of a known reader name you can request it with this command.
     * The SDK Wrapper will call [WorkflowCallbacks.onReader] as an answer.
     *
     * @param name Name of the reader.
     */
    fun getReader(name: String) {
        send(GetReader(name))
    }

    /**
     * Returns information about all connected readers.
     *
     * If you explicitly want to ask for information of all connected readers you can request it with this command.
     * The SDK Wrapper will call [WorkflowCallbacks.onReaderList] as an answer.
     */
    fun getReaderList() {
        send(GetReaderList())
    }

    /**
     * Pass a detected nfc tag to the [WorkflowController]
     *
     * Since only a foreground application can detect nfc tags,
     * you need to pass them to the SDK for it to handle detected id cards.
     *
     * @see NfcForegroundDispatcher
     *
     * @param tag Detected id card. ISO-DEP (ISO 14443-4) NFC tag
     */
    fun onNfcTagDetected(tag: Tag) {
        require(tag.techList.contains(IsoDep::class.java.name)) { "NFC tag isn't a ISO-DEP (ISO 14443-4) NFC tag" }

        SDKWrapper.launch(ioDispatcher) {
            if (isStarted) {
                sdkConnection.updateNfcTag(tag)
            } else {
                callback { onWrapperError(WrapperError("WorkflowController::onNfcTagDetected: isStarted", "Not started")) }
            }
        }
    }

    private inline fun <reified T : Command> send(command: T) =
        SDKWrapper.launch(ioDispatcher) {
            if (isStarted) {
                sdkConnection.send(command)
            } else {
                callback { onWrapperError(WrapperError("WorkflowController::send: isStarted", "Not started")) }
            }
        }

    private fun callback(callback: WorkflowCallbacks.() -> Unit) =
        SDKWrapper.launch(mainDispatcher) {
            workflowCallbacks.forEach {
                callback(it)
            }
        }

    private fun handleEnterPassword(message: Message) {
        val reader = message.getReaderFromReaderMember()
        if (reader == null) {
            callback { onWrapperError(WrapperError(message.msg.toString(), "Missing reader")) }
            return
        }

        when (message.msg) {
            MSG_ENTER_PIN -> {
                callback { onEnterPin(message.error, reader) }
            }
            MSG_ENTER_NEW_PIN -> {
                callback { onEnterNewPin(message.error, reader) }
            }
            MSG_ENTER_PUK -> {
                callback { onEnterPuk(message.error, reader) }
            }
            MSG_ENTER_CAN -> {
                callback { onEnterCan(message.error, reader) }
            }
            else -> {
                Log.d(TAG, "Received unknown enter password message ${message.msg}")
            }
        }
    }

    private fun handleMessage(message: Message) {
        when (message.msg) {
            MSG_INFO -> {
                when (val info = message.getVersionInfo()) {
                    null -> {
                        callback { onWrapperError(WrapperError(message.msg, "Parsing error")) }
                    }
                    else -> {
                        callback { onInfo(info) }
                    }
                }
            }
            MSG_AUTH -> {
                if (message.error != null) {
                    callback { onAuthenticationStartFailed(message.error) }
                }

                when (val authResult = message.getAuthResult()) {
                    null -> {
                        // Everything is fine, the SDK started the authentication workflow and sent an empty AUTH message.
                        callback { onAuthenticationStarted() }
                    }
                    else -> {
                        callback { onAuthenticationCompleted(authResult) }
                    }
                }
            }
            MSG_ACCESS_RIGHTS -> {
                when (val accessRights = message.getAccessRights()) {
                    null -> {
                        callback { onWrapperError(WrapperError(message.msg, "Missing access rights")) }
                    }
                    else -> {
                        callback { onAccessRights(message.error, accessRights) }
                    }
                }
            }
            MSG_BAD_STATE -> {
                callback { onWrapperError(WrapperError(message.msg, message.error ?: "Unknown bad state")) }
            }
            MSG_CHANGE_PIN -> {
                when (message.success) {
                    null -> {
                        callback { onChangePinStarted() }
                    }
                    else -> {
                        callback { onChangePinCompleted(ChangePinResult(message.success, message.reason)) }
                    }
                }
            }
            MSG_ENTER_PIN, MSG_ENTER_CAN, MSG_ENTER_PUK, MSG_ENTER_NEW_PIN -> {
                handleEnterPassword(message)
            }
            MSG_INSERT_CARD -> {
                callback { onInsertCard(null) }
            }
            MSG_CERTIFICATE -> {
                when (val certificateDescription = message.getCertificateDescription()) {
                    null -> {
                        callback { onWrapperError(WrapperError(message.msg, "Missing certificateDescription")) }
                    }
                    else -> {
                        callback { onCertificate(certificateDescription) }
                    }
                }
            }
            MSG_PAUSE -> {
                when (val cause = Cause.fromRawName(message.cause)) {
                    null -> {
                        callback { onWrapperError(WrapperError(message.msg, "Failed to map cause \"${message.cause}\" to PauseReason")) }
                    }
                    else -> {
                        callback { onPause(cause) }
                    }
                }
            }
            MSG_READER -> {
                callback { onReader(message.getReaderFromRoot()) }
            }
            MSG_READER_LIST -> {
                callback { onReaderList(message.getReaderList()) }
            }
            MSG_INVALID, MSG_UNKNOWN_COMMAND -> {
                val error = message.error ?: "Unknown SDK Wrapper error"
                callback { onWrapperError(WrapperError(message.msg, error)) }
            }
            MSG_INTERNAL_ERROR -> {
                val errorMessage = message.error ?: "Unknown internal error"
                callback { onInternalError(errorMessage) }
            }
            MSG_STATUS -> {
                callback { onStatus(message.getWorkflowProgress()) }
            }
            else -> {
                Log.d(TAG, "Received unknown message ${message.msg}")
            }
        }
    }

    companion object {
        private val TAG = WorkflowController::class.java.simpleName

        const val PIN_LENGTH = 6
        const val TRANSPORT_PIN_LENGTH = 5
        const val PUK_LENGTH = 10
        const val CAN_LENGTH = 6
    }
}
