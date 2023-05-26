package com.example.fuzzysearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.AutoCompleteTextView
import android.widget.EditText


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val autoCompleteTextView  = findViewById<AutoCompleteTextView>(R.id.searchEditText)
        autoCompleteTextView.addTextChangedListener(ContactSearchWatcher(this, autoCompleteTextView))
    }

}