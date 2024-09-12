-dontobfuscate
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations
-keep class androidx.databinding.** { *; }

-keep class com.governikus.ausweisapp.sdkwrapper.DataBinderMapperImpl { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.DataBinderMapperImpl$** { *; }

-keep class com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.** { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.CertificateDescription { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.CertificateValidity { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.AccessRights { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.AuxiliaryData { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.Card { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.Cause { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.Reader { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.Simulator { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.AuthResult { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.AuthResultData { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.VersionInfo { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowProgress { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.WrapperError { *; }
-keep class com.governikus.ausweisapp.sdkwrapper.card.core.ChangePinResult { *; }
-keep enum com.governikus.ausweisapp.sdkwrapper.card.core.AccessRight { *; }
-keep enum com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowProgressType { *; }

-keep class com.governikus.ausweisapp.sdkwrapper.card.core.NfcForegroundDispatcher { *; }

-keep public interface com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowCallbacks { *; }
-keep public class com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowController {
    public <methods>;
    public <fields>;
}

-keep class com.governikus.ausweisapp.sdkwrapper.SDKWrapper$** {
    public <methods>;
}
-keep class com.governikus.ausweisapp.sdkwrapper.SDKWrapper {
    public *;
}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
