package ru.skillbranch.skillarticles.viewmodels.base

import androidx.lifecycle.SavedStateHandle

//ViewModel сейчас могут сохранять свое состояние в SavedState
interface IViewModelState { // Просто операции с bundle
    fun save(outState: SavedStateHandle) {
        //default empty implementation
    }
    fun restore(savedState: SavedStateHandle) : IViewModelState {
        return this
    }
}