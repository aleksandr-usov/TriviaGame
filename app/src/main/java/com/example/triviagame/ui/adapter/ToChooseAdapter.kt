package com.example.triviagame.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.triviagame.R
import com.example.triviagame.ui.fragments.ToChooseFragment
import kotlinx.android.synthetic.main.list_item_element.view.*

class ToChooseAdapter(val listener: ToChooseFragment.OnListItemClickListener) :
    RecyclerView.Adapter<ToChooseAdapter.ToChooseViewHolder>() {

    private val listToChoose: MutableList<String> = mutableListOf()
    private var selected: String = ""

    fun setItems(newItems: List<String>) {
        listToChoose.clear()
        listToChoose.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setSelectedItem(selected: String) {
        this.selected = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ToChooseViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_element, parent, false)
        return ToChooseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToChooseViewHolder, position: Int) {
        val item = listToChoose[position]
        holder.bind(item, item == selected)
    }

    override fun getItemCount(): Int {
        return listToChoose.size
    }

    inner class ToChooseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView.tv_list_item
        private val ivSelected = itemView.iv_is_selected

        fun bind(item: String, isSelected: Boolean) {
            itemView.setOnClickListener { listener.onItemClick(item) }
            textView.text = item
            ivSelected.isVisible = isSelected
        }
    }
}