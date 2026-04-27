package com.example.bd1

import android.graphics.BitmapFactory
import android.net.Uri
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ProductAdapter(
    private val onItemClick: (ProductItem) -> Unit,
    private val onItemLongClick: (ProductItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val items = mutableListOf<ProductItem>()

    fun submitList(data: List<ProductItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(items[position], onItemClick, onItemLongClick)
    }

    override fun getItemCount(): Int = items.size

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCodigo: TextView = itemView.findViewById(R.id.tv_item_codigo)
        private val tvDiscount: TextView = itemView.findViewById(R.id.tv_item_discount)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tv_item_descripcion)
        private val tvOldPrice: TextView = itemView.findViewById(R.id.tv_item_old_price)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tv_item_precio)
        private val ivImagen: ImageView = itemView.findViewById(R.id.iv_item_producto)

        fun bind(
            item: ProductItem,
            onItemClick: (ProductItem) -> Unit,
            onItemLongClick: (ProductItem) -> Unit
        ) {
            val discount = ((item.codigo % 5) + 2) * 5
            val oldPrice = item.precio * (1 + (discount.toDouble() / 100.0))

            tvCodigo.text = "#${item.codigo}"
            tvDiscount.text = "-$discount%"
            tvDescripcion.text = item.descripcion
            tvOldPrice.text = "$${String.format(Locale.US, "%.2f", oldPrice)}"
            tvOldPrice.paintFlags = tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tvPrecio.text = "$${String.format(Locale.US, "%.2f", item.precio)}"

            cargarImagenSegura(item.imagenUri)

            itemView.setOnClickListener { onItemClick(item) }
            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }

        private fun cargarImagenSegura(uriText: String?) {
            if (uriText.isNullOrBlank()) {
                ivImagen.setImageResource(R.drawable.ic_image_placeholder)
                return
            }

            val result = runCatching {
                val uri = Uri.parse(uriText)
                itemView.context.contentResolver.openInputStream(uri)?.use {
                    val bitmap = BitmapFactory.decodeStream(it)
                    ivImagen.setImageBitmap(bitmap)
                } ?: throw IllegalArgumentException("No se pudo abrir la imagen")
            }

            if (result.isFailure) {
                ivImagen.setImageResource(R.drawable.ic_image_placeholder)
            }
        }
    }
}
