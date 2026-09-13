-keep class com.chuckerteam.chucker.internal.data.entity.HttpTransaction { *; }

# Gson
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
