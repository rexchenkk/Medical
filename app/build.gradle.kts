plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("kotlin-kapt")
}

android {
  namespace = "com.mobiuspace.medical"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.medical.expert"
    minSdk = 24
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
//    sourceCompatibility = VERSION_11
//    targetCompatibility = VERSION_11
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    viewBinding = true
  }
}

dependencies {

  implementation("androidx.core:core-ktx:1.9.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.10.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
  implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  // glide
  implementation ("com.github.bumptech.glide:glide:4.14.2")
  // gson
  implementation ("com.google.code.gson:gson:2.10")
  // picture selector
  // 请不要跨版本升级，请先查看Kotlin版Demo
  implementation ("io.github.lucksiege:pictureselector:v3.11.1")
  // 图片压缩 (按需引入)
  implementation ("io.github.lucksiege:compress:v3.11.1")
  // okhttp
  implementation ("com.squareup.okhttp3:okhttp:4.10.0")
  implementation("com.github.wanggaowan:PhotoPreview:2.4.7")
  implementation("androidx.room:room-runtime:2.2.3")
  kapt("androidx.room:room-compiler:2.2.3")
}