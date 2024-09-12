/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.nfc.Tag
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowController
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Command
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Message
import com.governikus.ausweisapp2.IAusweisApp2Sdk
import com.governikus.ausweisapp2.IAusweisApp2SdkCallback

internal class AA2SdkConnection : WorkflowController.SdkConnection {
    private var context: Context? = null
    private var sdkConnection: ServiceConnection? = null
    private var sdk: IAusweisApp2Sdk? = null
    private var sdkSessionId: String? = null

    private val gson = Gson()

    override val isConnected: Boolean
        get() = sdk != null

    override fun bind(
        context: Context,
        onConnected: (() -> Unit)?,
        onConnectionFailed: (() -> Unit)?,
        onMessageReceived: ((message: Message) -> Unit)?,
    ) {
        this.context = context.applicationContext
        val serviceIntent = Intent("com.governikus.ausweisapp2.START_SERVICE")
        serviceIntent.setPackage(context.applicationContext.packageName)

        val sdkCallback =
            object : IAusweisApp2SdkCallback.Stub() {
                override fun sessionIdGenerated(
                    sessionId: String,
                    isSecureSessionId: Boolean,
                ) {
                    this@AA2SdkConnection.sdkSessionId = sessionId
                }

                override fun receive(messageJson: String) {
                    Log.d(TAG, "Received message: $messageJson")

                    try {
                        val message = gson.fromJson(messageJson, Message::class.java)
                        onMessageReceived?.invoke(message)
                    } catch (e: JsonSyntaxException) {
                        Log.e(TAG, "Could not parse json message", e)
                    }
                }

                override fun sdkDisconnected() {
                    sdkSessionId = null
                }
            }

        sdkConnection =
            object : ServiceConnection {
                override fun onServiceConnected(
                    className: ComponentName,
                    service: IBinder,
                ) {
                    try {
                        sdk = IAusweisApp2Sdk.Stub.asInterface(service)
                        sdk?.connectSdk(sdkCallback)
                        onConnected?.invoke()
                    } catch (e: RemoteException) {
                        Log.d(TAG, "Could not connect to ausweisapp sdk", e)
                        onConnectionFailed?.invoke()
                    }
                }

                override fun onServiceDisconnected(className: ComponentName) {
                    Log.d(TAG, "Service disconnected")
                    sdk = null
                    sdkSessionId = null
                }
            }.apply {
                try {
                    context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
                } catch (e: SecurityException) {
                    Log.d(TAG, "Could not bind service", e)
                    onConnectionFailed?.invoke()
                }
            }
    }

    override fun unbind() {
        sdkConnection?.apply {
            context?.unbindService(this)
        }
        context = null
        sdkConnection = null
        sdk = null
        sdkSessionId = null
    }

    override fun <T : Command> send(command: T): Boolean {
        val sdk = sdk ?: return false
        val sessionId = sdkSessionId ?: return false

        return try {
            val messageJson = gson.toJson(command)
            sdk.send(sessionId, messageJson)
        } catch (e: RemoteException) {
            Log.d(TAG, "Could not send command", e)
            false
        }
    }

    override fun updateNfcTag(tag: Tag): Boolean {
        val sdk = sdk ?: return false
        val sessionId = sdkSessionId ?: return false

        return try {
            sdk.updateNfcTag(sessionId, tag)
            true
        } catch (e: RemoteException) {
            Log.d(TAG, "Could not update nfc tag", e)
            false
        }
    }

    companion object {
        private val TAG = ServiceConnection::class.java.simpleName
    }
}
