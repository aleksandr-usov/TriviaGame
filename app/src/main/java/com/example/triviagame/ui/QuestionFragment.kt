package com.example.triviagame.ui


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.triviagame.R
import kotlinx.android.synthetic.main.fragment_question.*

class QuestionFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_question, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
    }

    private fun initViews() {
        with(btn_next_question) {
            isEnabled = rb_answer_group.checkedRadioButtonId == -1
            
            setOnClickListener {
                rb_third_answer.visibility = View.VISIBLE
                rb_fourth_answer.visibility = View.VISIBLE

                viewModel.nextQuestion()
                rb_answer_group.clearCheck()
            }   
        }

        btn_end_game.setOnClickListener {
            val startFragment = StartFragment()
            fragmentManager?.beginTransaction()?.replace(R.id.fl_container, startFragment)?.commit()
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        with(viewModel) {
            secondsRemaining.observe(viewLifecycleOwner, Observer {
                tv_timer.text = it.toString()
            })

            questionSetupLiveData.observe(viewLifecycleOwner, Observer {
                it ?: return@Observer

                // hide progress bar

                rb_answer_group.setOnCheckedChangeListener { _, checkedId ->
                    btn_next_question.isEnabled = checkedId != -1
                }

                val answers = mutableListOf(it.question.correctAnswer)
                answers.addAll(it.question.incorrectAnswers)
                answers.shuffle()

                tv_question.text = it.question.question
                tv_category_question.text = it.question.category
                tv_difficulty_question.text = it.question.difficulty.capitalize()
                tv_number_of_question.text = "${it.index}/${it.questionsAmount}"
                rb_first_answer.text = answers[0]
                rb_second_answer.text = answers[1]

                if (it.question.type == "boolean") {
                    rb_third_answer.visibility = View.INVISIBLE
                    rb_fourth_answer.visibility = View.INVISIBLE
                    rb_third_answer.text = ""
                    rb_fourth_answer.text = ""
                } else {
                    rb_third_answer.visibility = View.VISIBLE
                    rb_fourth_answer.visibility = View.VISIBLE
                    rb_third_answer.text = answers[2]
                    rb_fourth_answer.text = answers[3]
                }
            })   
        }
    }
}
