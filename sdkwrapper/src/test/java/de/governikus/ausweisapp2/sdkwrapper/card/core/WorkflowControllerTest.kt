/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper.card.core

import android.content.Context
import android.net.Uri
import android.nfc.Tag
import com.google.gson.Gson
import de.governikus.ausweisapp2.sdkwrapper.SDKWrapper
import de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.Accept
import de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.Command
import de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.Message
import de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.RunAuth
import de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.SetPin
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WorkflowControllerTest {
    private var workflowController: WorkflowController? = null
    private var connection: MockSdkConnection? = null

    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        connection = MockSdkConnection()
        workflowController = WorkflowController(connection!!)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
        workflowController = null
        connection = null
    }

    @Test
    fun testOnStarted() =
        runTest(timeout = 1000.milliseconds) {
            assertNotNull(workflowController)
            val workflowController = workflowController!!

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onStarted() {
                                it.resume(true)
                            }
                        },
                    )

                    workflowController.start(RuntimeEnvironment.getApplication())
                }
            assert(completed)

            assert(workflowController.isStarted)
        }

    @Test
    fun testErrorNotStarted() =
        runTest(timeout = 1000.milliseconds) {
            assertNotNull(workflowController)
            val workflowController = workflowController!!

            assertFalse(workflowController.isStarted)

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onWrapperError(error: WrapperError) {
                                it.resume(true)
                            }
                        },
                    )

                    workflowController.startAuthentication(Uri.parse("https://test.test"))
                }
            assert(completed)
        }

    @Test
    fun testAuthenticationStarted() =
        runTest(timeout = 1000.milliseconds) {
            assertNotNull(workflowController)
            assertNotNull(connection)
            val workflowController = workflowController!!
            val connection = connection!!

            val testUrl = Uri.parse("https://test.test")

            connection.onCommandSend = {
                val command = it as? RunAuth

                assertNotNull(command)
                assertEquals(testUrl.toString(), command?.tcTokenURL)

                connection.receive("{\"msg\":\"AUTH\"}")
            }

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onAuthenticationStarted() {
                                it.resume(true)
                            }
                        },
                    )

                    workflowController.start(RuntimeEnvironment.getApplication())
                    workflowController.startAuthentication(testUrl)
                }

            assert(completed)
        }

    @Test
    fun testFullAuthentication() =
        runTest(timeout = 1000.milliseconds) {
            assertNotNull(workflowController)
            assertNotNull(connection)
            val workflowController = workflowController!!
            val connection = connection!!

            val tcTokenUrl = Uri.parse("https://test.test")
            val testPin = "123456"

            connection.onCommandSend = { command ->
                when (command) {
                    is RunAuth -> {
                        assertNotNull(command)
                        assertEquals(tcTokenUrl.toString(), command.tcTokenURL)

                        connection.receive("{\"msg\":\"AUTH\"}")
                    }
                    is Accept -> {
                        connection.receive("{\"msg\":\"INSERT_CARD\"}")
                    }
                    is SetPin -> {
                        assertEquals(testPin, command.value)

                        connection.receive(
                            "{" +
                                "  \"msg\": \"AUTH\"," +
                                "  \"result\":" +
                                "           {" +
                                "            \"major\": \"http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok\"" +
                                "           }," +
                                "  \"url\": \"https://test.governikus-eid.de/gov_autent/async?refID=_123456789\"" +
                                "}",
                        )
                    }
                    else -> {
                    }
                }
            }

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onAuthenticationStarted() {
                                connection.receive(
                                    "{" +
                                        "  \"msg\": \"ACCESS_RIGHTS\"," +
                                        "  \"aux\":" +
                                        "       {" +
                                        "        \"ageVerificationDate\": \"1999-07-20\"," +
                                        "        \"requiredAge\": \"18\"," +
                                        "        \"validityDate\": \"2017-07-20\"," +
                                        "        \"communityId\": \"02760400110000\"" +
                                        "       }," +
                                        "  \"chat\":" +
                                        "        {" +
                                        "         \"effective\": [\"Address\", \"FamilyName\", \"GivenNames\", \"AgeVerification\"]," +
                                        "         \"optional\": [\"GivenNames\", \"AgeVerification\"]," +
                                        "         \"required\": [\"Address\", \"FamilyName\"]" +
                                        "        }," +
                                        "  \"transactionInfo\": \"this is an example\"" +
                                        "}",
                                )
                            }

                            override fun onAccessRights(
                                error: String?,
                                accessRights: AccessRights?,
                            ) {
                                workflowController.accept()
                            }

                            override fun onInsertCard(error: String?) {
                                connection.receive(
                                    "{" +
                                        "  \"msg\": \"READER\"," +
                                        "  \"name\": \"NFC\"," +
                                        "  \"attached\": true," +
                                        "  \"insertable\": true," +
                                        "  \"keypad\": false," +
                                        "  \"card\":" +
                                        "         {" +
                                        "          \"inoperative\": false," +
                                        "          \"deactivated\": false," +
                                        "          \"retryCounter\": 3" +
                                        "         }" +
                                        "}",
                                )
                            }

                            override fun onReader(reader: Reader?) {
                                val card = reader?.card
                                assertNotNull(card)
                                assertEquals(3, card?.pinRetryCounter)
                                assertEquals(false, card?.inoperative)
                                assertEquals(false, card?.deactivated)

                                connection.receive(
                                    "{" +
                                        "  \"msg\": \"ENTER_PIN\"," +
                                        "  \"reader\":" +
                                        "           {" +
                                        "            \"name\": \"NFC\"," +
                                        "            \"attached\": true," +
                                        "            \"insertable\": true," +
                                        "            \"keypad\": false," +
                                        "            \"card\":" +
                                        "                   {" +
                                        "                    \"inoperative\": false," +
                                        "                    \"deactivated\": false," +
                                        "                    \"retryCounter\": 3" +
                                        "                   }" +
                                        "           }" +
                                        "}",
                                )
                            }

                            override fun onEnterPin(
                                error: String?,
                                reader: Reader,
                            ) {
                                val card = reader.card
                                if (card != null) {
                                    assertEquals(3, card.pinRetryCounter)
                                    assertEquals(false, card.inoperative)
                                    assertEquals(false, card.deactivated)

                                    workflowController.setPin(testPin)
                                } else {
                                    assert(false)
                                }
                            }

                            override fun onAuthenticationCompleted(authResult: AuthResult) {
                                assertNotNull(authResult.result)
                                assertNotNull(authResult.url)
                                assertEquals("http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok", authResult.result?.major)

                                it.resume(true)
                            }
                        },
                    )

                    workflowController.start(RuntimeEnvironment.getApplication())
                    workflowController.startAuthentication(tcTokenUrl)
                }

            assert(completed)
        }
}

internal class MockSdkConnection : WorkflowController.SdkConnection {
    private var onMessageReceived: ((message: Message) -> Unit)? = null

    var onCommandSend: ((command: Command) -> Unit)? = null

    override var isConnected: Boolean = false
        private set

    override fun bind(
        context: Context,
        onConnected: (() -> Unit)?,
        onConnectionFailed: (() -> Unit)?,
        onMessageReceived: ((message: Message) -> Unit)?,
    ) {
        this.onMessageReceived = onMessageReceived
        isConnected = true
        onConnected?.invoke()
    }

    override fun unbind() {
        onMessageReceived = null
        isConnected = false
    }

    override fun updateNfcTag(tag: Tag): Boolean {
        return false
    }

    override fun <T : Command> send(command: T): Boolean {
        SDKWrapper.launch {
            onCommandSend?.invoke(command)
        }
        return isConnected
    }

    fun receive(messageJson: String) {
        SDKWrapper.launch {
            val message = Gson().fromJson(messageJson, Message::class.java)
            onMessageReceived?.invoke(message)
        }
    }
}

internal open class TestWorkflowCallbacks : WorkflowCallbacks {
    override fun onStarted() {}

    override fun onAuthenticationStarted() {}

    override fun onAuthenticationStartFailed(error: String) {}

    override fun onChangePinStarted() {}

    override fun onAccessRights(
        error: String?,
        accessRights: AccessRights?,
    ) {}

    override fun onCertificate(certificateDescription: CertificateDescription) {}

    override fun onInsertCard(error: String?) {}

    override fun onReader(reader: Reader?) {}

    override fun onReaderList(readers: List<Reader>?) {}

    override fun onEnterPin(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterNewPin(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterPuk(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterCan(
        error: String?,
        reader: Reader,
    ) {}

    override fun onAuthenticationCompleted(authResult: AuthResult) {}

    override fun onChangePinCompleted(changePinResult: ChangePinResult) {}

    override fun onWrapperError(error: WrapperError) {}

    override fun onStatus(workflowProgress: WorkflowProgress) {}

    override fun onInfo(versionInfo: VersionInfo) {}

    override fun onBadState(error: String) {}

    override fun onInternalError(error: String) {}

    override fun onPause(cause: Cause) {}
}
