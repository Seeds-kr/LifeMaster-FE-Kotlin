package com.example.lifemaster.presentation.home.edit.view

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.HomeConfig
import com.example.lifemaster.presentation.home.edit.adapter.*
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.edit

class HomeEditActivity : AppCompatActivity(), OnStartDragListener {

    private lateinit var serviceAdapter: HomeEditAdapter
    private lateinit var allFeaturesAdapter: HomeEditAdapter
    private var serviceTouchHelper: ItemTouchHelper? = null
    private var allFeaturesTouchHelper: ItemTouchHelper? = null
    private val serviceList = mutableListOf<String>()
    private val allFeaturesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_edit)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<AppCompatButton>(R.id.btnSaveHomeEdit).setOnClickListener {
            saveHomeConfiguration()
            finish()
        }

        loadHomeConfiguration()

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
        } else {
            allFeaturesList.remove(item)
            serviceList.add(item)
        }
        serviceAdapter.notifyDataSetChanged()
        allFeaturesAdapter.notifyDataSetChanged()
    }

    private fun saveHomeConfiguration() {
        val prefs = getSharedPreferences("home_pref", Context.MODE_PRIVATE)
        prefs.edit {
            val nameToKey = HomeConfig.SERVICE_KEY_MAP
            val visibleKeys = serviceList.mapNotNull { nameToKey[it] }
            val allKeys = (serviceList + allFeaturesList).mapNotNull { nameToKey[it] }
            putStringSet("visible_components", visibleKeys.toSet())
            putString("component_order", allKeys.joinToString(","))
        }
    }

    private fun loadHomeConfiguration() {
        val prefs = getSharedPreferences("home_pref", Context.MODE_PRIVATE)
        val visible = prefs.getStringSet("visible_components", null)
        val order = prefs.getString("component_order", null)?.split(",") ?: HomeConfig.DEFAULT_ORDER

        serviceList.clear()
        allFeaturesList.clear()
        for (key in order) {
            val name = HomeConfig.KEY_TO_NAME[key]
            if (name != null) {
                if (visible?.contains(key) == true) {
                    serviceList.add(name)
                } else {
                    allFeaturesList.add(name)
                }
            }
        }
    }
}