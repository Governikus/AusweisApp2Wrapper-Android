# AusweisApp2 SDK Wrapper

The SDK Wrapper is an android library for providing a convenient frontend to the AusweisApp2.

## Installation

For now you need to include the aars in tester/libs in your project.
You also have to make sure to add all dependencies found in tester/build.gradle to your project.

## Usage

You can use the app as a simple wrapper of the ausweisapp2 sdk via the WorkflowController:

    import de.governikus.ausweiapp2.sdkwrapper.SDKWrapper

    val tcTokenUrl: Uri = ...
    SDKWrapper.workflowController.registerCallbacks(object : WorkflowCallbacks {
        override fun onStarted() {
            SDKWrapper.workflowController.startAuthentication(tcTokenUrl)
        }

        override fun onAuthenticationCompleted(authResult: AuthResult) {
            [...]
        }
    })

    SDKWrapper.workflowController.start()

## Contact

    Governikus GmbH & Co. KG.
    Hochschulring 4
    28359 Bremen

## License

Copyright (c) 2023 Governikus GmbH & Co. KG, Germany
