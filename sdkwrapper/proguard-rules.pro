-dontobfuscate
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations
-keep class androidx.databinding.** { *; }

-keep class de.governikus.ausweisapp2.sdkwrapper.DataBinderMapperImpl { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.DataBinderMapperImpl$** { *; }

-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.ausweisapp2.protocol.** { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.CertificateDescription { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.CertificateValidity { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.AccessRights { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.AuxiliaryData { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.Card { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.AuthResult { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.AuthResultData { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.WrapperError { *; }
-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.ChangePinResult { *; }
-keep enum de.governikus.ausweisapp2.sdkwrapper.card.core.AccessRight { *; }
-keep enum de.governikus.ausweisapp2.sdkwrapper.card.core.WorkflowProgressType { *; }

-keep class de.governikus.ausweisapp2.sdkwrapper.card.core.NfcForegroundDispatcher { *; }

-keep public interface de.governikus.ausweisapp2.sdkwrapper.card.core.WorkflowCallbacks { *; }
-keep public class de.governikus.ausweisapp2.sdkwrapper.card.core.WorkflowController {
    public <methods>;
    public <fields>;
}

-keep class de.governikus.ausweisapp2.sdkwrapper.SDKWrapper$** {
    public <methods>;
}
-keep class de.governikus.ausweisapp2.sdkwrapper.SDKWrapper {
    public *;
}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
