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
import androidx.navigation.Navigation
import com.example.licenta.R
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mView: View

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
        val wantToRegister = mView.findViewById<View>(R.id.registerAccount) as TextView
        wantToRegister.setOnClickListener {
            val navController =
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment)
            navController.navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        val loginButton = mView.findViewById<View>(R.id.login_button) as Button
        loginButton.setOnClickListener { login() }
    }

    private fun login() {
        val emailTxt = mView.findViewById<View>(R.id.emailEditTextLogin) as EditText
        val email = emailTxt.text.toString()
        val passwordTxt = mView.findViewById<View>(R.id.passwordEditTextLogin) as EditText
        val password = passwordTxt.text.toString()

        if (!email.isEmpty() && !password.isEmpty()) {
            activity?.let {
                mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    it
                ) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Successfully logged in!", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(context, "Error Logging in!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } else {
            Toast.makeText(context, "Please fill up the Credentials!", Toast.LENGTH_SHORT).show()
        }
    }
}