# iText / PDF
-keep class com.itextpdf.** { *; }
-keep interface com.itextpdf.** { *; }

# BouncyCastle if used by PDF signing/encryption
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Generated missing rules
-dontwarn aQute.bnd.annotation.spi.**
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**
-dontwarn com.itextpdf.bouncycastle.**
-dontwarn com.itextpdf.bouncycastlefips.**