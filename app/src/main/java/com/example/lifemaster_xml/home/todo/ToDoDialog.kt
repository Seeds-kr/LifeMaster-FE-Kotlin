package com.example.lifemaster_xml.home.todo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.lifemaster_xml.data.Datas
import com.example.lifemaster_xml.databinding.DialogTodoBinding

// [?] dialog 와 dialog fragment 의 차이?
class ToDoDialog(
    todoDialogInterface: ToDoDialogInterface
): DialogFragment() {
    lateinit var binding: DialogTodoBinding
    private var todoDialogInterface: ToDoDialogInterface? = null

    init {
        this.todoDialogInterface = todoDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnAddTodoItem.setOnClickListener {
            val content = binding.etTodoContent.text.toString()
            if(content.isBlank()) {
                Toast.makeText(requireContext(), "내용을 입력해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                Datas.todoItems.add(content)
                val sharedPreference = requireActivity().getSharedPreferences("todo_items", Context.MODE_PRIVATE)
                val editor = sharedPreference?.edit()
                editor?.putString("${Datas.todoItems.lastIndex}", content)
                editor?.commit()
                this.todoDialogInterface?.registerToDoItem() // [?] 꼭 인터페이스를 써야하는가?
                dismiss()
            }
        }
    }
}

interface ToDoDialogInterface {
    fun registerToDoItem()
}