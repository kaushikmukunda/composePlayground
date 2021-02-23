package com.km.composePlayground.customView

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.km.composePlayground.R

class CustomViewActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_custom)
    findViewById<ImageView>(R.id.image).load(R.drawable.squirrel) {
      crossfade(true)
    }
  }
}