package com.example.lifemaster.presentation.total

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.edit.adapter.HomeEditAdapter
import com.example.lifemaster.presentation.home.edit.adapter.HomeEditOnStartDragListener
import com.example.lifemaster.presentation.home.edit.adapter.SimpleItemTouchHelperCallback

class TotalServicesEditActivity : AppCompatActivity(), HomeEditOnStartDragListener {

    private lateinit var adapter: HomeEditAdapter
    private var touchHelper: ItemTouchHelper? = null
    private val orderedKeys = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_services_edit)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        orderedKeys.clear()
        orderedKeys.addAll(TotalServicesConfig.loadOrder(this))

        findViewById<AppCompatButton>(R.id.btnSaveTotalServicesEdit).setOnClickListener {
            TotalServicesConfig.persistOrder(this, orderedKeys.toList())
            finish()
        }

        val rv = findViewById<RecyclerView>(R.id.rvTotalServiceOrderList)
        adapter = HomeEditAdapter(
            itemList = orderedKeys,
            isServiceList = true,
            onToggleClick = { _, _ -> },
            startDragListener = this,
            showToggle = false,
            labelForItem = { getString(TotalServicesConfig.titleResForKey(it)) },
            iconTintForItem = TotalServicesConfig::homeEditTintHexForKey
        )
        rv.apply {
            layoutManager = LinearLayoutManager(this@TotalServicesEditActivity)
            this.adapter = this@TotalServicesEditActivity.adapter
            itemAnimator = null
        }
        touchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(adapter))
        touchHelper?.attachToRecyclerView(rv)
    }

    override fun onStartDrag(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (recyclerView.id == R.id.rvTotalServiceOrderList) {
            touchHelper?.startDrag(viewHolder)
        }
    }
}
