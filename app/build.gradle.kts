import java.util.Properties

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	id("com.google.gms.google-services")
	alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
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

		//load values from .properties file
		val keystoreFile = project.rootProject.file("keys.properties")
		val properties = Properties()
		properties.load(keystoreFile.inputStream())

		//return empty key in case something ggoes wrong
		val geminiApiKey = properties.getProperty("GEMINI_API_KEY") ?: ""
		buildConfigField(type = "String", name = "GEMINI_KEY", value = geminiApiKey)
		val ticketmasterApiKey = properties.getProperty("TICKETMASTER_API_KEY") ?: ""
		buildConfigField(type = "String", name = "TM_KEY", value = ticketmasterApiKey)
		val googleMapsApiKey = properties.getProperty("GOOGLE_MAPS_API_KEY") ?: ""
		manifestPlaceholders["GOOGLEMAPS_KEY"] = googleMapsApiKey
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
		buildConfig = true
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
	implementation(libs.play.services.maps)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}