# Types and members the JNI code resolves by name. The Android AAR ships this file as a consumer
# rule too.

# Type fully-qualified name is resolved from JNI code.
-keepnames class app.cash.zipline.MemoryUsage

# Type constructor is resolved from JNI code.
-keepclassmembers class app.cash.zipline.MemoryUsage {
  <init>(...);
}

# Type name and functions resolved from JNI code.
-keep interface app.cash.zipline.internal.bridge.CallChannel {
  *;
}

# Method names (onMessage, onTasksEnqueued) are resolved from JNI code.
-keep interface app.cash.zipline.CdpListener {
  *;
}
-keepclasseswithmembers class * implements app.cash.zipline.CdpListener {
  *;
}

# Type and method names are resolved from JNI code, with JsonElement in a method signature.
-keep interface app.cash.zipline.DirectChangeSink {
  *;
}
-keepnames class kotlinx.serialization.json.JsonElement

# Type name and constructor are resolved from JNI code to build modifier changes.
-keepnames class kotlin.Pair
-keepclassmembers class kotlin.Pair {
  <init>(java.lang.Object, java.lang.Object);
}
