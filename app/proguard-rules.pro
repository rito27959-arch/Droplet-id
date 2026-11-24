# Droplet ID — règles R8.
# Les bibliothèques (Compose, Glasmio, Tink, OkHttp, CameraX) apportent leurs
# propres règles. On ne garde ici que ce que la réflexion peut atteindre.

# Tink : les clés protobuf sont lues par réflexion.
-keep class com.google.crypto.tink.proto.** { *; }
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**

# OkHttp : plateformes optionnelles absentes d'Android.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Les journaux de débogage disparaissent de la version publiée.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
