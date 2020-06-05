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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.licenta.R
import com.example.licenta.Utils
import com.example.licenta.viewmodel.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var mView: View
    private lateinit var navController: NavController
    private lateinit var authenticationViewModel: AuthenticationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_login, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController =
            Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
        authenticationViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)
        authenticationViewModel.checkIfUserIsAuthenticated()
        authenticationViewModel.isUserAuthenticatedLiveData?.observe(
            viewLifecycleOwner,
            Observer { authenticated ->
                if (authenticated) {
                    navController.navigate(R.id.action_loginFragment_to_futureTripsFragment)
                }
                else{
                    Utils.helloUser = ""
                    FirebaseAuth.getInstance().signOut()
                }
            })
        val wantToRegister = mView.findViewById<View>(R.id.registerAccount) as TextView
        wantToRegister.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registrationFragment)
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
        authenticationViewModel.loginLiveData?.observe(viewLifecycleOwner,
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
                        navController.navigate(R.id.action_loginFragment_to_futureTripsFragment)
                        Toast.makeText(context, "Successfully logged in!", Toast.LENGTH_LONG)
                            .show()
                    }
                    else -> Toast.makeText(context, "Error Logging in!", Toast.LENGTH_SHORT).show()
                }
            })
    }
}