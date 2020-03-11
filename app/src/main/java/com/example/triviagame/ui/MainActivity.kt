package com.example.triviagame.ui

import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.triviagame.R
import com.example.triviagame.ui.SharedViewModel.GameScreen.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)

        viewModel.screen.observe(this, Observer {
            val fragment = when (it) {
                MAIN_MENU -> StartFragment()
                LIST -> ToChooseFragment()
                GAME -> QuestionFragment()
                FINISH -> ResultFragment()
            }

            supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fl_container, fragment)
                .commit()
        })

        viewModel.messages.observe(this, Observer {
            Toast.makeText(this, it, LENGTH_SHORT).show()
        })
    }
}
