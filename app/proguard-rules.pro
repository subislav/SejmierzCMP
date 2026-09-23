# =============================================================================
# Sejmierz R8 / ProGuard rules.
# The benchmarking suite runs on the optimized RELEASE build, so these rules
# must keep everything that relies on reflection from being stripped/renamed.
# =============================================================================

# ---- kotlinx.serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
# Keep generated serializers and @Serializable classes' companions.
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers, allowshrinking class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class pl.jsubczynski.sejmierz.**$$serializer { *; }
-keepclassmembers class pl.jsubczynski.sejmierz.** {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class pl.jsubczynski.sejmierz.** { *; }

# ---- Ktor ----
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# ---- OkHttp / Okio ----
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# ---- SQLDelight ----
-keep class app.cash.sqldelight.** { *; }
-dontwarn app.cash.sqldelight.**

# ---- Decompose / Essenty ----
-keep class com.arkivanov.decompose.** { *; }
-keep class com.arkivanov.essenty.** { *; }

# ---- Koin ----
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ---- Coroutines ----
-dontwarn kotlinx.coroutines.**

# Keep Sejmierz benchmark + DTO model classes intact for clean exports.
-keep class pl.jsubczynski.sejmierz.domain.benchmark.** { *; }
-keep class pl.jsubczynski.sejmierz.data.remote.dto.** { *; }
