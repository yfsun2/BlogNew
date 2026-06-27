plugins {
    id("com.android.application")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\22827\\.android\\key")
            storePassword = "jpfsfn74"
            keyAlias = "key-release"
            keyPassword = "123456"
        }
    }
    namespace = "com.syf.blognew"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.syf.blognew"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding=true
        buildConfig=true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    ndkVersion = "27.3.13750724"
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.squareup.okhttp3:okhttp:5.3.2")
    implementation ("org.projectlombok:lombok:1.18.44")
    annotationProcessor ("org.projectlombok:lombok:1.18.44")
    implementation ("com.alibaba:fastjson:2.0.30")
    //noinspection Aligned16KB
    implementation ("io.alterac.blurkit:blurkit:1.1.0")
    implementation ("org.java-websocket:Java-WebSocket:1.6.0")
    implementation ("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor ("com.github.bumptech.glide:compiler:5.0.5")
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.1")
    implementation("io.github.scwang90:refresh-header-classics:2.1.1")
    implementation("io.github.scwang90:refresh-footer-classics:2.1.1")
    implementation("com.github.donkingliang:ImageSelector:2.2.1")
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
//    implementation("cn.jiguang.im:chat:3.7.0")
//    implementation("cn.jiguang.im:ui:3.7.0")
}