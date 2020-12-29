package com.example.myfirstarfurnitureapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpBottomSheet()
    }

    private fun setUpBottomSheet(){
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50f,resources.displayMetrics).toInt()
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheet.bringToFront()
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

        })
    }
}