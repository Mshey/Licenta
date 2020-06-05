package com.example.licenta.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.licenta.R
import com.example.licenta.Utils.helloUser
import com.example.licenta.Utils.toPx
import com.example.licenta.Utils.username
import com.example.licenta.viewmodel.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_account.*


class AccountFragment : Fragment() {
    private lateinit var mView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_account, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (helloUser == "") {
            helloUser = "Hello, $username"
        }
        user_info.text = helloUser


        change_password.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                .navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }

        delete_account.setOnClickListener { delete() }

        val confidentialText =
            "When you’ve created this account you accepted the Terms and conditions of this application. These are mandatory requirements for using this app.\n" +
                    "If you don’t want to use your account anymore or you don’t agree with the terms and conditions, you can delete your account."

        val spannableString = SpannableString(confidentialText)

        val termsClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                    .navigate(R.id.action_accountFragment_to_termsFragment)
            }
        }

        val deleteClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                delete()
            }
        }

        spannableString.setSpan(termsClickableSpan, 50, 70, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(deleteClickableSpan, 249, 255, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val confidentialTextView: TextView = confidential_text
        confidentialTextView.text = spannableString
        confidentialTextView.movementMethod = LinkMovementMethod.getInstance()

        logout_button.setOnClickListener {
            helloUser = ""
            FirebaseAuth.getInstance().signOut()
            Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                .navigate(R.id.action_accountFragment_to_loginFragment)
        }
    }

    private fun delete() {
        AlertDialog.Builder(context)
            .setTitle("Deleting account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton(
                android.R.string.yes
            ) { _, _ ->
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                val lp = LinearLayout.LayoutParams(
                    50.toPx(),
                    15.toPx()
                )
                input.layoutParams = lp
                AlertDialog.Builder(context)
                    .setTitle("Password")
                    .setMessage("Please enter your password!")
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        val authenticationViewModel: AuthenticationViewModel =
                            ViewModelProvider(this).get(
                                AuthenticationViewModel::class.java
                            )
                        authenticationViewModel.deleteAccount(input.text.toString())
                        authenticationViewModel.deleteAccountLiveData?.observe(
                            viewLifecycleOwner,
                            Observer { del ->
                                if (del) {
                                    Toast.makeText(
                                        context,
                                        "Account deleted successfully!",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    Navigation.findNavController(
                                        requireActivity(),
                                        R.id.my_nav_host_fragment
                                    )
                                        .navigate(R.id.action_accountFragment_to_loginFragment)
                                } else Toast.makeText(
                                    context,
                                    "Something went wrong!",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            })
                    }
                    .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
                    .setView(input)
                    .show()
            }
            .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}