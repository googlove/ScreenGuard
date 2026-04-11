package com.screenguard.protector.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

// Extension function for view binding
inline fun <T : ViewBinding> ViewGroup.inflate(
    crossinline factory: (LayoutInflater, ViewGroup, Boolean) -> T
): T {
    return factory(LayoutInflater.from(context), this, false)
}
