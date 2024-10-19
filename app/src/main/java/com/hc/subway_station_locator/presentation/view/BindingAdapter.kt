package com.hc.subway_station_locator.presentation.view

import android.view.View
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hc.subway_station_locator.R
import com.hc.subway_station_locator.domain.model.SubwayStationVO

@BindingAdapter("isActivated")
fun isActivated(button: AppCompatButton, isActivated: Boolean) {
    if (isActivated) {
        button.isEnabled = true
        button.setTextColor(button.context.getColor(R.color.text_color_positive))
        button.setBackgroundResource(R.drawable.shape_r4_button_positive)
    } else {
        button.isEnabled = false
        button.setTextColor(button.context.getColor(R.color.text_color_negative))
        button.setBackgroundResource(R.drawable.shape_r4_button_negative)
    }
}

@BindingAdapter("submitList")
fun <T, VH: ViewHolder> submitList(recyclerView: RecyclerView, items: List<T>?) {
    (recyclerView.adapter as? ListAdapter<T, VH>)?.submitList(items)
}

@BindingAdapter("isVisible")
fun isVisible(view: View, isVisible: Boolean?) {
    isVisible ?: return

    view.isVisible = isVisible
}

@BindingAdapter("isInvisible")
fun isInvisible(view: View, isInvisible: Boolean?) {
    isInvisible ?: return

    view.isInvisible = isInvisible
}

@BindingAdapter("clearText")
fun clearText(editText: EditText, clearText: Boolean?) {
    clearText ?: return

    if (clearText) {
        editText.setText("")
    }
}

@BindingAdapter("background")
fun setBackground(view: View, @ColorRes background: Int?) {
    background ?: return

    view.setBackgroundResource(background)
}

@BindingAdapter("backgroundTint")
fun setBackgroundTint(view: View, @ColorRes background: Int?) {
    if (background == null || background == 0x0) return

    view.backgroundTintList = ContextCompat.getColorStateList(view.context, background)
}
