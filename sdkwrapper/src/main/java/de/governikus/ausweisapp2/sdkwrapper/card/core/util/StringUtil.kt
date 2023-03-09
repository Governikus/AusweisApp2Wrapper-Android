/**
 * Copyright (c) 2020-2023 Governikus GmbH & Co. KG, Germany
 */

package de.governikus.ausweisapp2.sdkwrapper.card.core.util

internal val NUMBER_REGEX = Regex("[0-9]+")

fun String.isNumber() = matches(NUMBER_REGEX)
