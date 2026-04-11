# ScreenGuard ProGuard Configuration

# Keep all ScreenGuard classes
-keep class com.screenguard.protector.** { *; }

# Keep Android framework classes
-keep public class android.** { *; }

# Keep AndroidX classes
-keep class androidx.** { *; }

# Keep Google Play Services classes
-keep class com.google.android.gms.** { *; }
-keep class com.google.mlkit.** { *; }

# Keep Material Design classes
-keep class com.google.android.material.** { *; }

# Keep Java classes for introspection
-keep class java.lang.reflect.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Room database classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep DataStore preferences
-keep class androidx.datastore.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep lifecycle classes
-keep class androidx.lifecycle.** { *; }

# Optimization settings
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Removing logging code
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Native methods should not be renamed
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep serialization classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
