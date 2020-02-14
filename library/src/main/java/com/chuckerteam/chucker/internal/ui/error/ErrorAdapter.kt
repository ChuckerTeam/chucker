package com.chuckerteam.chucker.internal.ui.error

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.data.entity.RecordedThrowableTuple
import java.text.DateFormat

internal class ErrorAdapter(
    val listener: ErrorClickListListener
) : RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder>() {

    private var data: List<RecordedThrowableTuple> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chucker_list_item_error, parent, false)
        return ErrorViewHolder(view)
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
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val tagView: TextView = itemView.findViewById(R.id.tag)
        private val clazzView: TextView = itemView.findViewById(R.id.clazz)
        private val messageView: TextView = itemView.findViewById(R.id.message)
        private val dateView: TextView = itemView.findViewById(R.id.date)
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
                listener.onErrorClick(it, adapterPosition)
            }
        }
    }

    private val RecordedThrowableTuple.formattedDate: String
        get() {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(this.date)
        }

    interface ErrorClickListListener {
        fun onErrorClick(throwableId: Long, position: Int)
    }
}
