package com.example.licenta.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.licenta.NavEvent
import com.example.licenta.R
import com.example.licenta.model.RegistrationState
import com.example.licenta.viewmodel.AuthenticationViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.fragment_registration.*
import javax.inject.Inject

@AndroidEntryPoint
class RegistrationFragment : Fragment() {
    @Inject
    lateinit var navEvents: PublishProcessor<NavEvent>

    private lateinit var mView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_registration, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val wantToLogin = mView.findViewById<View>(R.id.loginAccount) as TextView
        wantToLogin.setOnClickListener {
//            val navController =
//                Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
//            navController.navigate(R.id.action_registrationFragment_to_loginFragment)
            navEvents.onNext(NavEvent(NavEvent.Destination.LOGIN))
        }
        terms_text.setOnClickListener {
//            Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
////                .navigate(R.id.action_registrationFragment_to_termsFragment)
            navEvents.onNext(NavEvent(NavEvent.Destination.TERMS))
        }
        val registerButton = mView.findViewById<View>(R.id.register_button) as Button
        registerButton.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val emailTxt = mView.findViewById<View>(R.id.emailEditTextRegister) as EditText
        val usernameTxt = mView.findViewById<View>(R.id.userNameEditText) as EditText
        val passwordTxt = mView.findViewById<View>(R.id.passwordEditTextRegister) as EditText
        val confirmPasswordTxt =
            mView.findViewById<View>(R.id.confirmPasswordEditTextRegister) as EditText

        val email = emailTxt.text.toString().trim()
        val username = usernameTxt.text.toString()
        val password = passwordTxt.text.toString()
        val confirmPassword = confirmPasswordTxt.text.toString()

        val acceptTerms = mView.findViewById<View>(R.id.terms) as CheckBox

        val authenticationViewModel: AuthenticationViewModel = ViewModelProvider(this).get(
            AuthenticationViewModel::class.java
        )
        authenticationViewModel.register(
            email,
            username,
            password,
            confirmPassword,
            acceptTerms.isChecked
        )
        authenticationViewModel.registerLiveData?.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    RegistrationState.CREDENTIALS_NOT_OK -> {
                        Toast.makeText(
                            context,
                            "Please fill up the Credentials",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    RegistrationState.NOT_VALID_EMAIL -> {
                        Toast.makeText(
                            context,
                            "Make sure your email is correct!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    RegistrationState.NOT_ACCEPTED_TERMS -> {
                        Toast.makeText(
                            context,
                            "Please accept the terms and conditions!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    RegistrationState.PASSWORDS_NOT_EQUAL -> {
                        Toast.makeText(
                            context,
                            "The two passwords are not the same!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    RegistrationState.SUCCESSFUL -> {
                        Toast.makeText(
                            context,
                            "Successfully registered!",
                            Toast.LENGTH_LONG
                        ).show()
//                        Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
//                            .navigate(R.id.action_registrationFragment_to_loginFragment)
                        navEvents.onNext(NavEvent(NavEvent.Destination.LOGIN))
                    }
                    RegistrationState.UNSUCCESSFUL -> {
                        Toast.makeText(
                            context,
                            "Error registering, try again later!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    RegistrationState.USERNAME_EXISTS -> {
                        Toast.makeText(
                            context,
                            "Username already exists!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "Something went wrong!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }
}