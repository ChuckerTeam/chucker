package com.chuckerteam.chucker.internal.ui.error

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.databinding.ChuckerListItemErrorBinding
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple

internal class ErrorAdapter(
    val listener: ErrorClickListListener
) : RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder>() {

    private var data: List<RecordedThrowableTuple> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorViewHolder {
        val viewBinding = ChuckerListItemErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ErrorViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ErrorViewHolder, position: Int) {
        val throwable = data[position]
        holder.bind(throwable)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<RecordedThrowableTuple>) {
        this.data = data
        notifyDataSetChanged()
    }

    inner class ErrorViewHolder(
        private val itemBinding: ChuckerListItemErrorBinding
    ) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        private var throwableId: Long? = null

        init {
            itemView.setOnClickListener(this)
        }

        internal fun bind(throwable: RecordedThrowableTuple) = with(itemBinding) {
            throwableId = throwable.id

            chuckerItemErrorTag.text = throwable.tag
            chuckerItemErrorClazz.text = throwable.clazz
            chuckerItemErrorMessage.text = throwable.message
            chuckerItemErrorDate.text = throwable.formattedDate
        }

        override fun onClick(v: View) {
            throwableId?.let {
                listener.onErrorClick(it, adapterPosition)
            }
        }
    }

    interface ErrorClickListListener {
        fun onErrorClick(throwableId: Long, position: Int)
    }
}
