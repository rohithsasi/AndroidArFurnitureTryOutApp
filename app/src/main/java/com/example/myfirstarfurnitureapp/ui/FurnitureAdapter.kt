package com.example.myfirstarfurnitureapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstarfurnitureapp.R
import com.example.myfirstarfurnitureapp.models.FurnitureImage
import kotlinx.android.synthetic.main.item_furniture.view.*

const val SELECTED_MODES_COLOR = Color.YELLOW
const val UNSELECTED_MODES_COLOR = Color.LTGRAY

class FurnitureAdapter(val models: List<FurnitureImage>) :
    RecyclerView.Adapter<FurnitureAdapter.FurnitureViewHolder>() {

    val selectedFurniture: LiveData<FurnitureImage>
        get() = _selectedFurniture
    private var _selectedFurniture = MutableLiveData<FurnitureImage>()

    private var selectedIndex = 0;

    override fun getItemCount() = models.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FurnitureViewHolder {
        return FurnitureViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_furniture, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FurnitureViewHolder, position: Int) {
        if (selectedIndex == holder.layoutPosition) {
            holder.itemView.setBackgroundColor(SELECTED_MODES_COLOR)
            _selectedFurniture.value = models[holder.layoutPosition]
        } else {
            holder.itemView.setBackgroundColor(UNSELECTED_MODES_COLOR)
        }

        holder.itemView.apply {
            ivThumbnail.setImageResource(models[position].imageResId)
            tvTitle.text = models[position].title

            setOnClickListener {
                selectModes(holder)
            }
        }
    }

    private fun selectModes(holder: FurnitureViewHolder) {
        if (selectedIndex != holder.layoutPosition) {

            holder.itemView.setBackgroundColor(SELECTED_MODES_COLOR)
            selectedIndex = holder.layoutPosition
            _selectedFurniture.value = models[holder.layoutPosition]
        }

    }

    class FurnitureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}