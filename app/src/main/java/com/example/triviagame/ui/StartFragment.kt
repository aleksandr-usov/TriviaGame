package com.example.triviagame.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.triviagame.R
import kotlinx.android.synthetic.main.fragment_start.*

class StartFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        viewModel.numberOfQuestions.observe(viewLifecycleOwner, Observer {
            sb_number_of_questions.progress = it
            tv_number_of_questions.text = it.toString()
        })

        with(viewModel) {
            fetchAllCategories()

            currentDifficulty.observe(viewLifecycleOwner, Observer {
                tv_difficulty_description.text = it.displayableName
            })
            currentMode.observe(viewLifecycleOwner, Observer {
                tv_type_description.text = it.displayableName
            })
            currentCategory.observe(viewLifecycleOwner, Observer {
                tv_category_description.text = it.name
            })
        }
    }

    private fun initViews() {
        sb_number_of_questions.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setNumberOfQuestions(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        btn_play.setOnClickListener {
            viewModel.onPlayClicked(sb_number_of_questions.progress)
        }

        v_category_to_choose.setOnClickListener {
            viewModel.onCategoryChooserClick()
        }

        v_difficulty_to_choose.setOnClickListener {
            viewModel.onDifficultyChooserClick()
        }

        v_type_to_choose.setOnClickListener {
            viewModel.onGameModeChooserClick()
        }
    }
}
