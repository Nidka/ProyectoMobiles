package com.example.bd1.feature.products.ui.adapter

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bd1.R
import com.example.bd1.feature.products.domain.model.Product
import java.util.Locale

class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onItemLongClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val items = mutableListOf<Product>()

    fun submitList(data: List<Product>) {
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
            item: Product,
            onItemClick: (Product) -> Unit,
            onItemLongClick: (Product) -> Unit
        ) {
            val discount = (item.id.hashCode() % 5 + 2) * 5
            val oldPrice = item.price * (1 + (discount.toDouble() / 100.0))

            tvCodigo.text = "#${item.id.take(6)}"
            tvDiscount.text = "-$discount%"
            tvDescripcion.text = item.name
            tvOldPrice.text = "$${String.format(Locale.US, "%.2f", oldPrice)}"
            tvOldPrice.paintFlags = tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tvPrecio.text = "$${String.format(Locale.US, "%.2f", item.price)}"

            cargarImagenSegura(item.imageUri)

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
