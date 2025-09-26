package com.en.teach

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

abstract class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable immersive status bar
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }
    
    protected fun setupImmersiveToolbar(toolbar: Toolbar) {
        val statusBarHeight = getStatusBarHeight()
        toolbar.setPadding(
            toolbar.paddingLeft,
            toolbar.paddingTop + statusBarHeight,
            toolbar.paddingRight,
            toolbar.paddingBottom
        )
    }
    
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}