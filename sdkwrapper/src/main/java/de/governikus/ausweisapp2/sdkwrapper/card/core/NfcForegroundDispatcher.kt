/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper.card.core

import android.app.Activity
import android.nfc.NfcAdapter.FLAG_READER_NFC_A
import android.nfc.NfcAdapter.FLAG_READER_NFC_B
import android.nfc.NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
import android.nfc.NfcAdapter.getDefaultAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep

/**
 * ForegroundDispatcher used to detect ISO-DEP (ISO 14443-4) NFC tags, like an id card.
 *
 * Use it to detect and pass detected id cards to the [WorkflowController.onNfcTagDetected] function.
 *
 * @param activity Activity that the NFCAdapter will be attached to
 * @param workflowController Workflow manager that is th receive the nfc tags
 */
class NfcForegroundDispatcher(
    private val activity: Activity,
    private val workflowController: WorkflowController,
) {
    private val nfcAdapter by lazy {
        getDefaultAdapter(activity)
    }
    private var isStarted = false
    private val nfcReaderFlags =
        FLAG_READER_NFC_A or FLAG_READER_NFC_B or FLAG_READER_SKIP_NDEF_CHECK
    private val nfcTechnology = IsoDep::class.java.name

    fun start() {
        if (isStarted) {
            return
        }
        isStarted = true
        nfcAdapter.enableReaderMode(
            activity,
            { tag: Tag ->
                if (tag.techList.contains(nfcTechnology)) {
                    workflowController.onNfcTagDetected(tag)
                }
            },
            nfcReaderFlags,
            null,
        )
    }

    /**
     * Stop the [NfcForegroundDispatcher]
     *
     * Must be called in [Activity.onPause], when the activity put into the background
     */
    fun stop() {
        if (!isStarted) {
            return
        }
        isStarted = false
        nfcAdapter.disableReaderMode(activity)
    }
}
