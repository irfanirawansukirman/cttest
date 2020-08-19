package com.irfanirawansukirman.githubsearch.mvvm.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.irfanirawansukirman.extensions.widget.load
import com.irfanirawansukirman.githubsearch.R
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ITEM_TYPE
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.PROGRESS_TYPE
import com.irfanirawansukirman.githubsearch.data.remote.response.Item
import com.irfanirawansukirman.githubsearch.databinding.MainItemBinding
import com.irfanirawansukirman.githubsearch.databinding.ProgressBinding

class MainAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Item>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE -> ItemHolder(
                MainItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            PROGRESS_TYPE -> ProgressHolder(
                ProgressBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw RuntimeException("viewType error")
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return when (data[position].viewType) {
            ITEM_TYPE -> ITEM_TYPE
            PROGRESS_TYPE -> PROGRESS_TYPE
            else -> throw RuntimeException("viewType error")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE -> (holder as ItemHolder).bindItem(data[holder.adapterPosition])
            PROGRESS_TYPE -> {
            }
            else -> throw RuntimeException("viewType error")
        }
    }

    inner class ItemHolder(private val mainItemBinding: MainItemBinding) :
        RecyclerView.ViewHolder(mainItemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun bindItem(item: Item) {
            mainItemBinding.apply {
                imgMain.load(item.avatarUrl, progress, R.color.gray, R.color.gray)
                txtMainTime.text = "02 Oktober 1992"
                txtMainTitle.text = item.login
            }
        }
    }

    inner class ProgressHolder(progressBinding: ProgressBinding) :
        RecyclerView.ViewHolder(progressBinding.root)

    fun addAllData(data: List<Item>) {
        val lastPosition = this.data.size.minus(1)
        this.data.addAll(data)
        notifyItemRangeInserted(lastPosition, data.size.minus(1))
    }

    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }

    fun addProgressFooter() {
        if (!isLoading()) {
            data.add(Item())
            notifyItemInserted(data.size.minus(1))
        }
    }

    fun removeProgressFooter() {
        if (isLoading()) {
            data.removeAt(data.size.minus(1))
            notifyItemRemoved(data.size.minus(1))
        }
    }

    private fun isLoading(): Boolean {
        return if (data.isNotEmpty()) {
            data.last().viewType == PROGRESS_TYPE
        } else {
            false
        }
    }
}