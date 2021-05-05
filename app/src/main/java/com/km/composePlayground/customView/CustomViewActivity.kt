package com.km.composePlayground.customView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.km.composePlayground.R

class CustomViewActivity : AppCompatActivity() {

  private var adapter: CustomAdapter? = null
  private val items = mutableListOf<Item>().apply {
    for (idx in 0..40) {
      add(Item("title $idx", "subtitle $idx"))
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    rvMode()
//    composeMode()
  }

  private fun composeMode() {
    setContent {
//      LazyColumn {
//        itemsIndexed(items) { _, item ->
          ItemUi(item = items[0])
//        }
//      }
    }
  }

  private fun rvMode() {
    setContentView(R.layout.activity_custom_rv)

    val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
    adapter = CustomAdapter(items)
    recyclerView.layoutManager = LinearLayoutManager(this)
    recyclerView.adapter = adapter
  }
}

internal data class Item(
  val title: String,
  val subtitle: String
)

internal class ComposeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  private val composeContainer: ComposeView = view.findViewById(R.id.compose_container)

  fun bind(item: Item) {
    composeContainer.setContent {
      ItemUi(item = item)
    }
  }
}

@Composable
private fun ItemUi(item: Item) {
  Column(modifier= Modifier.padding(24.dp)) {
    Log.d("dbg", "composing $item")
    Text(item.title)
    Text(item.subtitle)
  }
}

internal class CustomAdapter(
  private val items: List<Item>
) : RecyclerView.Adapter<ComposeViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    val view = layoutInflater.inflate(R.layout.compose_container, parent, false)
    return ComposeViewHolder(view)
  }

  override fun onBindViewHolder(holder: ComposeViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount(): Int {
    return items.size
  }
}