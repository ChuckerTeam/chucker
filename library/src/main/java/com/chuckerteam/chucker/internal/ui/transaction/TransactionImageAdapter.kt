package com.chuckerteam.chucker.internal.ui.transaction

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemImageBinding
import com.chuckerteam.chucker.internal.support.ChessboardDrawable

internal class TransactionImageAdapter : RecyclerView.Adapter<TransactionImageAdapter.ImageViewHolder>() {

    private lateinit var image: Bitmap
    private var luminance: Double? = null

    fun setImageItem(image: Bitmap, luminance: Double?) {
        this.image = image
        this.luminance = luminance
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ChuckerTransactionItemImageBinding.inflate(inflater, parent, false)
        return ImageViewHolder(itemBinding)
    }

    override fun getItemCount(): Int = if (!this::image.isInitialized) 0 else 1

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(image, luminance)
    }

    internal class ImageViewHolder(
        private val imageBinding: ChuckerTransactionItemImageBinding
    ) : RecyclerView.ViewHolder(imageBinding.root) {

        fun bind(item: Bitmap, luminance: Double?) {
            imageBinding.binaryData.setImageBitmap(item)
            imageBinding.root.background = createContrastingBackground(luminance)
        }

        private fun createContrastingBackground(luminance: Double?): Drawable? {
            if (luminance == null) return null

            return if (luminance < LUMINANCE_THRESHOLD) {
                ChessboardDrawable.createPattern(
                    itemView.context,
                    R.color.chucker_chessboard_even_square_light,
                    R.color.chucker_chessboard_odd_square_light,
                    R.dimen.chucker_half_grid
                )
            } else {
                ChessboardDrawable.createPattern(
                    itemView.context,
                    R.color.chucker_chessboard_even_square_dark,
                    R.color.chucker_chessboard_odd_square_dark,
                    R.dimen.chucker_half_grid
                )
            }
        }

        private companion object {
            const val LUMINANCE_THRESHOLD = 0.25
        }
    }
}
