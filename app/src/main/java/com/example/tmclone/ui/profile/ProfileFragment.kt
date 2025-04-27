package com.example.tmclone.ui.profile

import android.R.id.input
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.tmclone.LoginActivity
import com.example.tmclone.R
import com.example.tmclone.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import org.w3c.dom.Text

private const val TAG = "ProfileFragment"
class ProfileFragment : Fragment() {


	lateinit var profileImage: ImageView
	lateinit var profileName: TextView
	lateinit var profileEmail: TextView
	lateinit var changePasswordButton: Button
	lateinit var logoutButton: Button
	lateinit var deleteAccountButton: Button

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
		deleteAccountButton = view.findViewById(R.id.deleteAcc_button)


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

		return view

	}

	private fun startLoginActivity() {
		val intent = Intent(activity, LoginActivity::class.java)
		startActivity(intent)
		requireActivity().finish()
	}

	// on logout click
	fun logout(view: View) {
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

	fun changePassword(view: View) {
		val currUser = Firebase.auth.currentUser
		//reauthenticate user

		//show dialog for user input
	}

	fun deletePassword(view: View){
		//reauthenticate user
		//show dialog prompting user if they are sure
	}


	//helper function
	//to be called before deleting or changing password
	private fun reauthenticateUser(){
		val currUser = Firebase.auth.currentUser
//		val credential = EmailAuthProvider
//			.getCredential(currUser.email, )
	}


	private fun dialogForChangePassword(){
		val builder = AlertDialog.Builder(requireContext())
			.setTitle("Change password")

		val passwordInput: EditText

		builder.setPositiveButton("Done") { dialog, which ->

		}
		builder.setNegativeButton("Cancel") {dialog, which ->
			//dismiss dialog
		}

	}
}
