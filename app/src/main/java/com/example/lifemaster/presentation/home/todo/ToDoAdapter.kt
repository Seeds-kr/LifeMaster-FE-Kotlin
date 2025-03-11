package com.example.lifemaster.presentation.home.todo

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemTodoBinding
import com.example.lifemaster.model.TodoItem
import com.example.lifemaster.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToDoAdapter(private val context: Context): ListAdapter<TodoItem, ToDoAdapter.ToDoViewHolder>(differ) {
    inner class ToDoViewHolder(private val binding: ItemTodoBinding): RecyclerView.ViewHolder(binding.root) {
//        val content: TextView
//        val goToPomodoro: ImageView
//        init {
//            content = itemView.findViewById(R.id.tv_todo_item)
//            goToPomodoro = itemView.findViewById(R.id.iv_go_to_pomodoro)
//
//            goToPomodoro.setOnClickListener {
////                // 1. home tab -> total view 전환
////                activity.findViewById<BottomNavigationView>(R.id.navigation).selectedItemId = R.id.action_total
////                // 2. total view 중 포모도로 프래그먼트 화면에 보이기
////                activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, PomodoroFragment(), "PomodoroFragment").commit()
//            }
//        }

        fun bind(item: TodoItem) = with(binding) {
            tvTitle.text = item.title
            itemView.setOnLongClickListener {
                showDialog(currentList[adapterPosition].id)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        return ToDoViewHolder(ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private fun showDialog(deleteItemId: Int) {
        AlertDialog.Builder(context)
            .setTitle("할일 목록 삭제")
            .setMessage("할일 목록을 삭제하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                RetrofitInstance.networkService.deleteTodoItem(deleteItemId).enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if(response.isSuccessful) {
                            Toast.makeText(context, "할일이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("server success", "else")
                        }
                    }
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Log.d("server error", ""+t.message)
                    }
                })
            }
            .setNegativeButton("아니요", null)
            .show()
    }

    companion object {
        val differ = object: DiffUtil.ItemCallback<TodoItem>() {
            override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}