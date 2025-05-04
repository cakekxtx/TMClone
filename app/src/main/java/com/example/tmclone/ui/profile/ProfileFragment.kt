package com.example.tmclone.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tmclone.LoginActivity
import com.example.tmclone.R
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


private const val TAG = "ProfileFragment"
class ProfileFragment : Fragment() {


	lateinit var profileImage: ImageView
	lateinit var profileName: TextView
	lateinit var profileEmail: TextView
	lateinit var changePasswordButton: Button
	lateinit var logoutButton: ImageButton
	lateinit var deleteAccountButton: Button

	var isChangingPassword: Boolean = false

	@SuppressLint("MissingInflatedId")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_profile, container, false)

		profileName = view.findViewById(R.id.profileName_textview)
		profileImage = view.findViewById(R.id.profileImage_imageView)
		profileEmail = view.findViewById(R.id.email_textview)
		changePasswordButton = view.findViewById(R.id.changePassword_button)
		logoutButton = view.findViewById(R.id.logoutButton)


		//instance of FirebaseAuth
		val currUser = FirebaseAuth.getInstance().currentUser
		if (currUser == null) {
			startLoginActivity()
		} else {
			//display user info
			profileName.text = currUser.displayName
			profileEmail.text = currUser.email

			Glide.with(this)
				.load(currUser.photoUrl)
				.placeholder(R.drawable.ic_launcher_foreground)
				.circleCrop()
				.into(profileImage)
		}

		logoutButton.setOnClickListener {
			logout()
		}

		changePasswordButton.setOnClickListener {
			dialogForChangePassword(requireActivity())
		}
		return view

	}

	private fun startLoginActivity() {
		val intent = Intent(activity, LoginActivity::class.java)
		startActivity(intent)
		requireActivity().finish()
	}

	// on logout click
	fun logout() {
		AuthUI.getInstance().signOut(requireContext())
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					//after logout start login activity
					startLoginActivity()
				} else {
					Log.e(TAG, "Task is not successful: ${task.exception}",)
				}
			}

	}



	//helper function
	//to be called before deleting or changing password
	private fun reauthenticateUserAndUpdate(currPass: String, newPass: String, context: Context){
		val currUser = FirebaseAuth.getInstance().currentUser!!

		if(isChangingPassword){
			val credential = when(currUser.providerId){
				"google.com" -> GoogleAuthProvider.getCredential(currUser.email.toString(), currPass)
				else -> EmailAuthProvider.getCredential(currUser.email.toString(), currPass)
			}

			// prompt user to provide login credentials
			currUser.reauthenticate(credential)
				.addOnCompleteListener {
					Log.d(TAG, "User reauthenticated successfully!")
					//update password
					helperToUpdatePassword(newPass, context)
				}
				.addOnFailureListener { e->
					Log.e(TAG, "Failed to reauthenticate user", e)

				}

		}


	}


	@SuppressLint("RestrictedApi")
	private fun dialogForChangePassword(context: Context){
		//

		val linearlayout = LinearLayout(context)

		val currPasswordEditText = EditText(context)
		currPasswordEditText.setHint("Current Password")
		val newPasswordEditText = EditText(context)
		newPasswordEditText.setHint("New Password")

		linearlayout.setOrientation(LinearLayout.VERTICAL)
		linearlayout.addView(currPasswordEditText)
		linearlayout.addView(newPasswordEditText)

		val builder = AlertDialog.Builder(context)
			.setTitle("Change password")
			.setMessage("Please enter your current and new password below to continue.")
			.setView(linearlayout,10,10,10,10)

		builder.setPositiveButton("Confirm") { dialog, which ->
			val currPassword = currPasswordEditText.text.toString()
			val newPassword = newPasswordEditText.text.toString()
			isChangingPassword = true
			reauthenticateUserAndUpdate(currPassword, newPassword, context)


		}
		builder.setNegativeButton("Cancel") {dialog, which ->
			Log.d("ChangePassword", "User cancelled password change.")
			isChangingPassword = false
		}

		builder.create().show()

	}

	private fun helperToUpdatePassword(newPass: String, context: Context){
		val currUser = FirebaseAuth.getInstance().currentUser!!

		currUser.updatePassword(newPass)
			.addOnCompleteListener { task ->
				if(task.isSuccessful){
					Toast.makeText(context, "Password successfully updated", Toast.LENGTH_SHORT).show()
					isChangingPassword = false
				}
			}
	}




}
