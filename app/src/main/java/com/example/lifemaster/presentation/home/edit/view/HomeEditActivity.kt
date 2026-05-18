package com.example.lifemaster.presentation.home.edit.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.edit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.HomeConfig
import com.example.lifemaster.presentation.home.edit.adapter.*

class HomeEditActivity : AppCompatActivity(), HomeEditOnStartDragListener {

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

        val rvService = findViewById<RecyclerView>(R.id.rvServiceList)
        serviceAdapter = HomeEditAdapter(
            itemList = serviceList,
            isServiceList = true,
            onToggleClick = ::onToggleClick,
            startDragListener = this
        )
        rvService.apply {
            layoutManager = LinearLayoutManager(this@HomeEditActivity)
            adapter = serviceAdapter
        }
        serviceTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(serviceAdapter))
        serviceTouchHelper?.attachToRecyclerView(rvService)

        val rvAllFeatures = findViewById<RecyclerView>(R.id.rvAllFeaturesList)
        allFeaturesAdapter = HomeEditAdapter(
            itemList = allFeaturesList,
            isServiceList = false,
            onToggleClick = ::onToggleClick,
            startDragListener = this
        )
        rvAllFeatures.apply {
            layoutManager = LinearLayoutManager(this@HomeEditActivity)
            adapter = allFeaturesAdapter
            itemAnimator = null
        }
        allFeaturesTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(allFeaturesAdapter))
        allFeaturesTouchHelper?.attachToRecyclerView(rvAllFeatures)
    }

    override fun onStartDrag(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        when (recyclerView.id) {
            R.id.rvServiceList -> serviceTouchHelper?.startDrag(viewHolder)
            R.id.rvAllFeaturesList -> allFeaturesTouchHelper?.startDrag(viewHolder)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onToggleClick(item: String, isServiceList: Boolean) {
        if (isServiceList) {
            serviceList.remove(item)
            if (!allFeaturesList.contains(item)) {
                allFeaturesList.add(item)
            }
        } else {
            allFeaturesList.remove(item)
            if (!serviceList.contains(item)) {
                serviceList.add(item)
            }
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
        val order = prefs.getString("component_order", null)?.split(",")
            ?: HomeConfig.DEFAULT_ORDER

        serviceList.clear()
        allFeaturesList.clear()

        val visibleKeys = visible ?: HomeConfig.DEFAULT_VISIBLE
        for (key in order) {
            val name = HomeConfig.KEY_TO_NAME[key]
            if (name != null) {
                if (visibleKeys.contains(key)) {
                    serviceList.add(name)
                } else {
                    allFeaturesList.add(name)
                }
            }
        }
    }
}