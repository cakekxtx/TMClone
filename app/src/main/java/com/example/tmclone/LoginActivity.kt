package com.example.tmclone

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginActivity"
class LoginActivity : AppCompatActivity() {
	@SuppressLint("MissingInflatedId")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_login)

		//instance of current user
		val currUser = FirebaseAuth.getInstance().currentUser

		//intent for main activity
		val mainActivityIntent = Intent(this, MainActivity::class.java)

		//if currUser not null, go to main activity
		if (currUser != null){
			startActivity(mainActivityIntent)
			finish()
		}
		else{
			//ActivityResultLauncher to launch sign-in activity
			val signActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
				if(result.resultCode == Activity.RESULT_OK){
					//user signed in successfully / new user
					val user = FirebaseAuth.getInstance().currentUser
					Log.d(TAG, "current user: $user")

					var toastMsg = ""
					//if new user
					if(user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp){
						//might have a preferences screen
						toastMsg = "Welcome!"
					}
					//if old user
					else{
						toastMsg = "Welcome back!"
					}

					Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()

					startActivity(mainActivityIntent)
					finish()
				}
				else{
					//sign in failled
					val response = IdpResponse.fromResultIntent(result.data)
					if(response == null){
						Log.d(TAG, "user has cancelled sign in request")
					}
					else{
						Log.e(TAG, "${response.error?.errorCode}", )
					}
				}


			}

			//when sign in button is clicked
			findViewById<Button>(R.id.signin_button).setOnClickListener {
				//auth providers
				val providers = arrayListOf(
					AuthUI.IdpConfig.EmailBuilder().build(),
					AuthUI.IdpConfig.GoogleBuilder().build()
				)

				//sign in intent
				val signInIntent = AuthUI.getInstance()
					.createSignInIntentBuilder()
					.setAvailableProviders(providers)
					.setTosAndPrivacyPolicyUrls("example.com", "example.com")
					.setLogo(R.drawable.ic_launcher_foreground)
					.setAlwaysShowSignInMethodScreen(true)
					.setIsSmartLockEnabled(false)
					.setTheme(com.firebase.ui.auth.R.style.FirebaseUI)
					.build()

				signActivityLauncher.launch(signInIntent)
			}
		}


	}
}