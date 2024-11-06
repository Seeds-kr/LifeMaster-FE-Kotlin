package com.example.lifemaster_xml.total.pomodoro

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.data.Datas
import com.example.lifemaster_xml.databinding.FragmentPomodoroBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PomodoroFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PomodoroFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentPomodoroBinding
    private val sharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(PomodoroViewModel::class.java) // https://black-jin0427.tistory.com/389
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivOpenTodoDialog.setOnClickListener {
            val dialog = PomodoroDialog()
            dialog.isCancelable = false
            dialog.show(activity?.supportFragmentManager!!, "PomodoroDialog")
        }
        // 문제 원인) fragment 에서 observing 을 못하고 있다.
        // 원인 예측) live data 를 setting 해주는 곳에서 쓰는 뷰모델 객체와 여기서 observing 하는 뷰모델 객체가 서로 다르다.
        sharedViewModel.selectedPosition.observe(viewLifecycleOwner) { selectedPosition ->
            binding.tvSelectTodoItem.text = Datas.todoItems[selectedPosition]
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PomodoroFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PomodoroFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}