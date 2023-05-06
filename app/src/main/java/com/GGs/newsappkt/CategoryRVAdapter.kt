package com.GGs.newsappkt

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CategoryRVAdapter : RecyclerView.Adapter<CategoryRVAdapter.ViewHolder> {
    private var categoryRVModals: ArrayList<CategoryRVModal>
    private var context: Context
    private var categoryClickInterface: CategoryClickInterface? = null

    constructor(
        categoriesRVModals: ArrayList<CategoryRVModal>,
        context: Context,
        categoryClickInterface: CategoryClickInterface?
    ) {
        categoryRVModals = categoriesRVModals
        this.context = context
        this.categoryClickInterface = categoryClickInterface
    }

    constructor(categoriesRVModals: ArrayList<CategoryRVModal>, context: Context) {
        categoryRVModals = categoriesRVModals
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.categories_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val categoryRVModal = categoryRVModals[position]
        holder.categoryTV.text = categoryRVModal.category
        Picasso.get().load(categoryRVModal.categoryImageUrl).into(holder.categoryIV)
        holder.itemView.setOnClickListener { categoryClickInterface!!.onCategoryClick(position) }
    }

    override fun getItemCount(): Int {
        return categoryRVModals.size
    }

    interface CategoryClickInterface {
        fun onCategoryClick(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTV: TextView
        val categoryIV: ImageView

        init {
            categoryTV = itemView.findViewById(R.id.txvCategoria)
            categoryIV = itemView.findViewById(R.id.ivCategoria)
        }
    }
}