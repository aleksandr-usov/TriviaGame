package com.example.triviagame.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.triviagame.R
import com.example.triviagame.ui.viewmodel.SharedViewModel
import kotlinx.android.synthetic.main.fragment_result.*

class ResultFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        viewModel.numberOfCorrectAnswers.observe(viewLifecycleOwner, Observer {
            tv_correct_number.text = it.toString()
        })

        btn_new_game.setOnClickListener {
            viewModel.screen.value = SharedViewModel.GameScreen.MAIN_MENU
        }
    }
}
