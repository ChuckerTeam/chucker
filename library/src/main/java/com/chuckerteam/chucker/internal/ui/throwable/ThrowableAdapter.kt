package com.chuckerteam.chucker.internal.ui.throwable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.databinding.ChuckerListItemThrowableBinding
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple
import java.text.DateFormat

internal class ThrowableAdapter(
    val listener: ThrowableClickListListener
) : RecyclerView.Adapter<ThrowableAdapter.ThrowableViewHolder>() {

    private var data: List<RecordedThrowableTuple> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThrowableViewHolder {
        val viewBinding = ChuckerListItemThrowableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThrowableViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ThrowableViewHolder, position: Int) {
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

    inner class ThrowableViewHolder(
        private val itemBinding: ChuckerListItemThrowableBinding
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
                listener.onThrowableClick(it, adapterPosition)
            }
        }
    }

    interface ThrowableClickListListener {
        fun onThrowableClick(throwableId: Long, position: Int)
    }
}
