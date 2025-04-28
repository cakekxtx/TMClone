plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	id("com.google.gms.google-services")
}

android {
	namespace = "com.example.tmclone"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.example.tmclone"
		minSdk = 24
		targetSdk = 35
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
	//Firebase BOM
	implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
	//Firebase products
	implementation("com.google.firebase:firebase-analytics")
	implementation("com.google.firebase:firebase-firestore-ktx") //Firestore
	implementation("com.google.firebase:firebase-auth-ktx") //Firebase Authentication
	implementation("com.firebaseui:firebase-ui-auth:8.0.2") //Firebase Auth UI
	// GSON
	implementation("com.google.code.gson:gson:2.12.1")
	implementation("com.squareup.retrofit2:converter-gson:2.11.0")
	// Retrofit
	implementation("com.squareup.retrofit2:retrofit:2.11.0")
	//Glide
	implementation("com.github.bumptech.glide:glide:4.16.0")

	//Gemini API
	implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.lifecycle.livedata.ktx)
	implementation(libs.androidx.lifecycle.viewmodel.ktx)
	implementation(libs.androidx.navigation.fragment.ktx)
	implementation(libs.androidx.navigation.ui.ktx)
	implementation(libs.androidx.legacy.support.v4)
	implementation(libs.androidx.fragment.ktx)
	implementation(libs.androidx.activity)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}