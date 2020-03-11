package com.example.triviagame.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.triviagame.R
import com.example.triviagame.ui.adapter.ToChooseAdapter
import com.example.triviagame.ui.viewmodel.SharedViewModel
import kotlinx.android.synthetic.main.fragment_to_choose.*

class ToChooseFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel

    private val listAdapter =
        ToChooseAdapter(object :
            OnListItemClickListener {
            override fun onItemClick(item: String) {
                viewModel.onListItemClicked(item)
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_to_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        viewModel.selectedListItem.observe(viewLifecycleOwner, Observer {
            listAdapter.setSelectedItem(it)
        })

        viewModel.listToChooseFrom.observe(viewLifecycleOwner, Observer {
            listAdapter.setItems(it ?: listOf())
        })

        rv_to_choose.adapter = listAdapter

        rv_to_choose.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                RecyclerView.VERTICAL
            )
        )
    }

    interface OnListItemClickListener {
        fun onItemClick(item: String)
    }
}