package com.example.tmclone.ui.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tmclone.R
import com.example.tmclone.databinding.FragmentBookmarksBinding

class BookmarksFragment : Fragment() {


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_bookmarks, container, false)
		return view
	}


}