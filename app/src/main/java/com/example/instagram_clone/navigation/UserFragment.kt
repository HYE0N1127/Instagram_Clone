package com.example.instagram_clone.navigation

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.instagram_clone.R

class UserFragment {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(Activity).inflate(R.layout.fragment_user, container, false)
    }
}