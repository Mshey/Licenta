package com.example.licenta.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.licenta.R
import com.example.licenta.Utils
import com.example.licenta.model.NavEvent
import com.example.licenta.viewmodel.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.processors.PublishProcessor
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {
    @Inject
    lateinit var navEvents: PublishProcessor<NavEvent>

    private lateinit var mView: View
    private lateinit var authenticationViewModel: AuthenticationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_login, container, false)
        authenticationViewModel =
            ViewModelProvider(requireActivity()).get(AuthenticationViewModel::class.java)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authenticationViewModel.checkIfUserIsAuthenticated()
        authenticationViewModel.isUserAuthenticatedLiveData.observe(
            viewLifecycleOwner,
            Observer { authenticated ->
                if (authenticated) {
                    navEvents.onNext(
                        NavEvent(
                            NavEvent.Destination.FUTURE
                        )
                    )

                } else {
                    Utils.helloUser = ""
                    FirebaseAuth.getInstance().signOut()
                }
            })
        val wantToRegister = mView.findViewById<View>(R.id.registerAccount) as TextView
        wantToRegister.setOnClickListener {
            navEvents.onNext(
                NavEvent(
                    NavEvent.Destination.REGISTRATION
                )
            )
        }

        val loginButton = mView.findViewById<View>(R.id.login_button) as Button
        loginButton.setOnClickListener { login() }
    }

    private fun login() {
        val emailTxt = mView.findViewById<View>(R.id.emailEditTextLogin) as EditText
        val email = emailTxt.text.toString().trim()
        val passwordTxt = mView.findViewById<View>(R.id.passwordEditTextLogin) as EditText
        val password = passwordTxt.text.toString()

        authenticationViewModel.login(email, password)
        authenticationViewModel.loginLiveData.observe(viewLifecycleOwner,
            Observer { canLogin ->
                when {
                    canLogin == null -> {
                        Toast.makeText(
                            context,
                            "Please fill up the Credentials!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    canLogin -> {
                        navEvents.onNext(
                            NavEvent(
                                NavEvent.Destination.FUTURE
                            )
                        )
                        Toast.makeText(context, "Successfully logged in!", Toast.LENGTH_LONG)
                            .show()
                    }
                    else -> Toast.makeText(context, "Error Logging in!", Toast.LENGTH_SHORT).show()
                }
            })
    }
}