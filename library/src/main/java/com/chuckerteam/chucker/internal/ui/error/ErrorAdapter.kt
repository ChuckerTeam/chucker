package com.chuckerteam.chucker.internal.ui.error

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.databinding.ChuckerListItemErrorBinding
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple
import java.text.DateFormat

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

            tag.text = throwable.tag
            clazz.text = throwable.clazz
            message.text = throwable.message
            date.text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(throwable.date)
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
