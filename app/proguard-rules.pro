# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\work\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-ignorewarnings
-verbose
-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keepattributes SourceFile,LineNumberTable
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep class support.design.widget.**{*;}
-keep class android.support.v7.widget.**{*;}
-keep class android.support.design.widget.**{*;}

-keepclassmembers class ** {
    public void onEvent*(**);
}

#libs/okhttp-2.5.0.jar
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.**{*;}

#com.rengwuxian.materialedittext
-dontwarn com.rengwuxian.materialedittext.**
-keep class com.rengwuxian.materialedittext.**{*;}

#com.nineoldandroids
-dontwarn com.nineoldandroids.**
-keep class com.nineoldandroids.**{*;}

#com.nineoldandroids
-dontwarn org.apache.commons.httpclient.**
-keep class org.apache.commons.httpclient.**{*;}
-dontwarn org.apache.jackrabbit.webdav.**
-keep class org.apache.jackrabbit.webdav.**{*;}

#EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }


#ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

#rxjava
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
