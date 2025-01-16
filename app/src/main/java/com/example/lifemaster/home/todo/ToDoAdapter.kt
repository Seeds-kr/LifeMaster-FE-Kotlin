package com.example.lifemaster.home.todo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.total.pomodoro.PomodoroFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ToDoAdapter(
    val todoItems: MutableList<String>,
    val context: Context,
    val activity: FragmentActivity // 단일 액티비티 사용
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView
        val goToPomodoro: ImageView
        init {
            content = itemView.findViewById(R.id.tv_todo_item)
            goToPomodoro = itemView.findViewById(R.id.iv_go_to_pomodoro)

            goToPomodoro.setOnClickListener {
                // 1. home tab -> total view 전환
                activity.findViewById<BottomNavigationView>(R.id.navigation).selectedItemId = R.id.action_total
                // 2. total view 중 포모도로 프래그먼트 화면에 보이기
                activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, PomodoroFragment(), "PomodoroFragment").commit()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false) // [!] 무조건 false 를 전달해야한다.
        return ToDoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.content.text = todoItems[position]
    }

    override fun getItemCount(): Int {
        return todoItems.size
    }
}