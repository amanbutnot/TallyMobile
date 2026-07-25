# R8 Missing Classes Fix - Suppression of warnings for missing desktop-only APIs
-dontwarn aQute.bnd.annotation.spi.ServiceConsumer
-dontwarn aQute.bnd.annotation.spi.ServiceProvider
-dontwarn com.itextpdf.bouncycastle.BouncyCastleFactory
-dontwarn com.itextpdf.bouncycastlefips.BouncyCastleFipsFactory
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**

# Keep itextpdf - Essential for PDF generation
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Keep apache.poi - Essential for Excel generation
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.schemas.**

# Keep FileKit - Essential for file saving and sharing
-keep class io.github.vinceglb.filekit.** { *; }

# Keep app classes used for sharing, expect/actual, and models
-keep class org.prime.easykarobar.data.expect.** { *; }
-keep class org.prime.easykarobar.data.model.** { *; }
-keep class org.prime.easykarobar.ui.shared.reportsShared.** { *; }
-keep class org.prime.easykarobar.AppContextHolder { *; }
-keep class org.prime.easykarobar.MainActivity { *; }

# Kotlin Serialization - Prevent stripping of serialized fields
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class kotlinx.serialization.** { *; }

# Voyager - Keep screens and models
-keep class cafe.adriel.voyager.** { *; }
