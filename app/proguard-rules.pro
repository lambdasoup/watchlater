-dontobfuscate

# retrofit
# http://square.github.io/retrofit/
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# okio
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# gson
# http://google-gson.googlecode.com/svn/trunk/examples/android-proguard-example/proguard.cfg
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# retrolambda
-dontwarn java.lang.invoke.*

# android m doesn't have apache anymore
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn retrofit.android.AndroidApacheClient
-dontwarn retrofit.client.ApacheClient**