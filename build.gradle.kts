// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.1" apply false

    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(platform("io.netty:netty-bom:4.1.137.Final"))
        constraints {
            add("classpath", "com.google.protobuf:protobuf-java:3.25.8")
            add("classpath", "commons-io:commons-io:2.14.0")
            add("classpath", "org.apache.commons:commons-compress:1.26.2")
            add("classpath", "org.bitbucket.b_c:jose4j:0.9.6")
            add("classpath", "org.jdom:jdom2:2.0.6.1")
        }
        val nav_version = "2.7.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}