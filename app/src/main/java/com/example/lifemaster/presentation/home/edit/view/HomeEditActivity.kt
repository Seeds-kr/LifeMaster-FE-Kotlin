package com.example.lifemaster.presentation.home.edit.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.edit.adapter.*
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton

class HomeEditActivity : AppCompatActivity(), OnStartDragListener {

    private lateinit var serviceAdapter: HomeEditAdapter
    private lateinit var allFeaturesAdapter: HomeEditAdapter
    private var serviceTouchHelper: ItemTouchHelper? = null
    private var allFeaturesTouchHelper: ItemTouchHelper? = null
    private val serviceList = mutableListOf("수면", "집중 시간", "그룹 바로가기", "자아성찰 바로가기")
    private val allFeaturesList = mutableListOf("앱 잠금 설정", "자유 게시판", "개선 게시판", "알람 추가")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_edit)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val btnSave = findViewById<AppCompatButton>(R.id.btnSaveHomeEdit)
        btnSave.setOnClickListener {
            finish()
        }

        serviceAdapter = HomeEditAdapter(serviceList, true, ::onToggleClick, this)
        findViewById<RecyclerView>(R.id.rvServiceList).apply {
            layoutManager = LinearLayoutManager(this@HomeEditActivity)
            adapter = serviceAdapter
        }
        serviceTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(serviceAdapter))
        serviceTouchHelper?.attachToRecyclerView(findViewById(R.id.rvServiceList))

        allFeaturesAdapter = HomeEditAdapter(allFeaturesList, false, ::onToggleClick, this)
        findViewById<RecyclerView>(R.id.rvAllFeaturesList).apply {
            layoutManager = LinearLayoutManager(this@HomeEditActivity)
            adapter = allFeaturesAdapter
        }
        allFeaturesTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(allFeaturesAdapter))
        allFeaturesTouchHelper?.attachToRecyclerView(findViewById(R.id.rvAllFeaturesList))
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        serviceTouchHelper?.startDrag(viewHolder)
        allFeaturesTouchHelper?.startDrag(viewHolder)
    }

    private fun onToggleClick(item: String, isServiceList: Boolean) {
        if (isServiceList) {
            serviceList.remove(item)
            allFeaturesList.add(item)
            serviceAdapter.notifyDataSetChanged()
            allFeaturesAdapter.notifyDataSetChanged()
        } else {
            allFeaturesList.remove(item)
            serviceList.add(item)
            allFeaturesAdapter.notifyDataSetChanged()
            serviceAdapter.notifyDataSetChanged()
        }
    }
}