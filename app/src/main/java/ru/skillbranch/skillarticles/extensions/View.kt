package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import androidx.navigation.NavDestination
import com.google.android.material.bottomnavigation.BottomNavigationView

//Реализуй функцию расширения View.setMarginOptionally(left:Int = marginLeft, top : Int = marginTop,
// right : Int = marginRight, bottom : Int = marginBottom), в качестве аргумента принимает значения
// внешних отступов View (margin) в пикселях
fun View.setMarginOptionally(left:Int = marginLeft, top : Int = marginTop, right : Int = marginRight, bottom : Int = marginBottom) {
    val layoutParams = (this.layoutParams  as? ViewGroup.MarginLayoutParams)
    layoutParams ?.bottomMargin = bottom
    layoutParams ?.topMargin = top
    layoutParams ?.rightMargin = right
    layoutParams ?.leftMargin = left
}


fun View.setPaddingOptionally(left:Int = paddingLeft, top : Int = paddingTop, right : Int = paddingRight, bottom : Int = paddingBottom) {
    setPadding(left, top, right, bottom)
}

fun BottomNavigationView.selectDestination(dest: NavDestination) {
    menu.findItem(dest.id)
        ?.let {
            it.isChecked = true } ?:
    run {
        menu.children.last().isChecked = true }
}

fun BottomNavigationView.selectItem(itemId: Int?){
    itemId?.let{
        menu.findItem(itemId)?.let{
            it.isChecked = true
        }
    }
}