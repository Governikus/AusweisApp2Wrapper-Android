/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol

internal interface Command {
    val cmd: String
}

internal class Accept : Command {
    override val cmd = "ACCEPT"
}

internal class Cancel : Command {
    override val cmd = "CANCEL"
}

internal class ContinueWorkflow : Command {
    override val cmd = "CONTINUE"
}

internal class GetCertificate : Command {
    override val cmd = "GET_CERTIFICATE"
}

internal class RunAuth(val tcTokenURL: String, val developerMode: Boolean, val status: Boolean) : Command {
    override val cmd = "RUN_AUTH"
}

internal class RunChangePin(val status: Boolean) : Command {
    override val cmd = "RUN_CHANGE_PIN"
}

internal class GetAccessRights : Command {
    override val cmd = "GET_ACCESS_RIGHTS"
}

internal class SetAccessRights(val chat: List<String>) : Command {
    override val cmd = "SET_ACCESS_RIGHTS"
}

internal class SetCan(val value: String?) : Command {
    override val cmd = "SET_CAN"
}

internal class SetCard(val name: String, val simulator: Simulator?) : Command {
    override val cmd = "SET_CARD"
}

internal class SetPin(val value: String?) : Command {
    override val cmd = "SET_PIN"
}

internal class SetNewPin(val value: String?) : Command {
    override val cmd = "SET_NEW_PIN"
}

internal class SetPuk(val value: String?) : Command {
    override val cmd = "SET_PUK"
}

internal class GetStatus : Command {
    override val cmd = "GET_STATUS"
}

internal class GetInfo : Command {
    override val cmd = "GET_INFO"
}

internal class GetReader(val name: String) : Command {
    override val cmd = "GET_READER"
}

internal class GetReaderList : Command {
    override val cmd = "GET_READER_LIST"
}
