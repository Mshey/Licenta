package com.example.licenta.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.licenta.R
import com.example.licenta.viewmodel.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_change_password.*

class ChangePasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        change_password_button.setOnClickListener {
            val authenticationViewModel: AuthenticationViewModel = ViewModelProvider(this).get(
                AuthenticationViewModel::class.java
            )
            authenticationViewModel.changePassword(
                oldPasswordEditTextRegister.text.toString(),
                newPasswordEditTextRegister.text.toString()
            )
            authenticationViewModel.changePasswordLiveData?.observe(
                viewLifecycleOwner,
                Observer { pass ->
                    if (pass) {
                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_LONG)
                            .show()
                        FirebaseAuth.getInstance().signOut()
                        Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                            .navigate(R.id.action_changePasswordFragment_to_loginFragment)
                    } else Toast.makeText(context, "Something went wrong!", Toast.LENGTH_LONG)
                        .show()
                })
        }
    }
}