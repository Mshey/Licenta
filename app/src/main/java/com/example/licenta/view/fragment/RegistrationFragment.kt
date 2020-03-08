package com.example.licenta.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.licenta.R
import com.google.firebase.auth.FirebaseAuth

class RegistrationFragment : Fragment() {
    private var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
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
            val navController =
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment)
            navController.navigate(R.id.action_registrationFragment_to_loginFragment)
        }
        val registerButton = mView.findViewById<View>(R.id.register_button) as Button
        registerButton.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val emailTxt = mView.findViewById<View>(R.id.emailEditTextRegister) as EditText
        val passwordTxt = mView.findViewById<View>(R.id.passwordEditTextRegister) as EditText
        val confirmPasswordtxt =
            mView.findViewById<View>(R.id.confirmPasswordEditTextRegister) as EditText

        val email = emailTxt.text.toString()
        val password = passwordTxt.text.toString()
        val confirmPassword = confirmPasswordtxt.text.toString()

        val acceptTerms = mView.findViewById<View>(R.id.terms) as CheckBox

        if (!email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
            if (isEmailValid(email)) {
                if (acceptTerms.isChecked) {
                    if (password.equals(confirmPassword)) {
                        activity?.let {
                            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(it) { task ->
                                    if (task.isSuccessful) {
                                        val user = mFirebaseAuth.currentUser
                                        val uid = user!!.uid
//                            mDatabase.child(uid).child("Name").setValue(confirmPassword)
                                        Toast.makeText(
                                                context,
                                                "Successfully registered!",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                        Log.d("RF", "success")
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error registering, try again later!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        Log.d("RF", "error")
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "The two passwords are not the same!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please accept the terms and conditions!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Make sure your email is correct!",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(context, "Please fill up the Credentials", Toast.LENGTH_LONG).show()
            Log.d("RF", "fill up")
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}