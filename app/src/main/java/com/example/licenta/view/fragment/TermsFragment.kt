package com.example.licenta.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.licenta.R
import kotlinx.android.synthetic.main.fragment_terms_and_conditions.*

class TermsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_terms_and_conditions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        agree_button.setOnClickListener {
//            Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
//                .navigate(R.id.action_termsFragment_to_registrationFragment)
//        }
    }
}