package com.example.triviagame.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.triviagame.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_start.*

class StartFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel

    private val difficultyToChoose =
        mapOf("Any" to "", "Easy" to "easy", "Medium" to "medium", "Hard" to "hard")
    private val typeToChoose =
        mapOf("Any" to "", "Multiple Choice" to "multiple", "True / False" to "boolean")

    private val adapter by lazy {
        return@lazy ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        )
    }

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
        viewModel.fetchAllCategories()

        viewModel.categoriesLiveData.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer

            val names = it.map { it.name }

            adapter.clear()
            adapter.add("Any")
            adapter.addAll(names)
            adapter.notifyDataSetChanged()
        })
    }

    private fun initViews() {

        sb_number_of_questions.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tv_number_of_questions.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        btn_play.setOnClickListener {
            viewModel.onPlayClicked(
                sb_number_of_questions.progress,
                sp_category.selectedItem.toString(),
                difficultyToChoose.getValue(sp_difficulty.selectedItem.toString()),
                typeToChoose.getValue(sp_type.selectedItem.toString())
            )

            val questionFragment = QuestionFragment()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fl_container, questionFragment)
            transaction?.commit()
        }

        val adapter1 = ArrayAdapter(
            activity as MainActivity,
            android.R.layout.simple_spinner_item,
            difficultyToChoose.keys.toList()
        )
        val adapter2 =
            ArrayAdapter(
                activity as MainActivity,
                android.R.layout.simple_spinner_item,
                typeToChoose.keys.toList()
            )

        sp_category.adapter = adapter
        sp_difficulty.adapter = adapter1
        sp_type.adapter = adapter2
    }
}
