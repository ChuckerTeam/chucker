package com.chuckerteam.chucker.internal.ui.throwable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple
import java.text.DateFormat

internal class ThrowableAdapter(
    val listener: ThrowableClickListListener
) : RecyclerView.Adapter<ThrowableAdapter.ThrowableViewHolder>() {

    private var data: List<RecordedThrowableTuple> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThrowableViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chucker_list_item_throwable, parent, false)
        return ThrowableViewHolder(view)
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
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val tagView: TextView = itemView.findViewById(R.id.chuckerItemThrowableTag)
        private val clazzView: TextView = itemView.findViewById(R.id.chuckerItemThrowableClazz)
        private val messageView: TextView = itemView.findViewById(R.id.chuckerItemThrowableMessage)
        private val dateView: TextView = itemView.findViewById(R.id.chuckerItemThrowableDate)
        private var throwableId: Long? = null

        init {
            itemView.setOnClickListener(this)
        }

        internal fun bind(throwable: RecordedThrowableTuple) = with(throwable) {
            throwableId = id
            tagView.text = tag
            clazzView.text = clazz
            messageView.text = message
            dateView.text = formattedDate
        }

        override fun onClick(v: View) {
            throwableId?.let {
                listener.onThrowableClick(it, adapterPosition)
            }
        }
    }

    private val RecordedThrowableTuple.formattedDate: String
        get() {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(this.date)
        }

    interface ThrowableClickListListener {
        fun onThrowableClick(throwableId: Long, position: Int)
    }
}
