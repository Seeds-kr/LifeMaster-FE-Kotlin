package com.example.lifemaster_xml.home.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
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
    ): View? {
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
//                Datas.todoItems.add(content) // [?] 원래 이 방식으로 하려다가 notifyDataSetChanged 가 안될 것 같아서 적용안했다. 해당 방식으로는 어려운건가?
                this.todoDialogInterface?.registerToDoItem(content) // [?] 데이터를 전달하기 위해 꼭 인터페이스를 써야하는가?
                dismiss()
            }
        }
    }
}

interface ToDoDialogInterface {
    fun registerToDoItem(content: String)
}