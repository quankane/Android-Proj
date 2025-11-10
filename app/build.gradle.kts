plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.android_proj"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.android_proj"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    //noinspection UseTomlInstead
    implementation("com.tbuonomo:dotsindicator:5.1.0")

    //noinspection NewerVersionAvailable
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    //noinspection NewerVersionAvailable
    implementation(platform("com.google.firebase:firebase-bom:33.0.0")) // Sử dụng phiên bản BOM mới nhất

    // Thư viện Xác thực và Firestore (KHÔNG CẦN SỐ PHIÊN BẢN)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx") // Dùng bản Kotlin (ktx) cho Storage

    // Glide (Thư viện Tải ảnh)
    // Cập nhật lên phiên bản ổn định mới nhất (4.16.0)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Annotation Processor (Bắt buộc cho Glide)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Gson (Phân tích JSON)
    implementation("com.google.code.gson:gson:2.10.1") // Giảm xuống 2.10.1 (phổ biến) hoặc giữ 2.13.2 nếu đã hoạt động

    // OkHttp (HTTP Networking)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}