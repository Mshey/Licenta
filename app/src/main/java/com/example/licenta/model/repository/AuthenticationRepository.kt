package com.example.licenta.model.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.licenta.Utils.ACCOUNTS
import com.example.licenta.Utils.TAG
import com.example.licenta.Utils.USERS
import com.example.licenta.Utils.isEmailValid
import com.example.licenta.Utils.username
import com.example.licenta.model.RegistrationState
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AuthenticationRepository {
    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firebaseReference = FirebaseDatabase.getInstance().reference

    fun checkIfUserIsAuthenticatedInFirebase(): MutableLiveData<Boolean>? {
        val isUserAuthenticateInFirebaseMutableLiveData: MutableLiveData<Boolean> =
            MutableLiveData<Boolean>()
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
        if (firebaseUser == null) {
            isUserAuthenticateInFirebaseMutableLiveData.setValue(false)
        } else {
            isUserAuthenticateInFirebaseMutableLiveData.value = true
            firebaseReference.child(ACCOUNTS)
                .child(firebaseAuth.currentUser?.uid!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        username = dataSnapshot.value as String
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.d(TAG, "onCancelled: " + p0.message)
                    }
                })
        }
        return isUserAuthenticateInFirebaseMutableLiveData
    }

    fun loginFirebase(email: String, password: String): MutableLiveData<Boolean>? {
        val loginFirebaseMutableLiveData: MutableLiveData<Boolean> =
            MutableLiveData<Boolean>()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseReference.child(ACCOUNTS)
                            .child(firebaseAuth.currentUser?.uid!!)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    username = dataSnapshot.value as String
                                }

                                override fun onCancelled(p0: DatabaseError) {
                                    Log.d(TAG, "onCancelled: " + p0.message)
                                }
                            })
                        loginFirebaseMutableLiveData.setValue(true)
                    } else {
                        loginFirebaseMutableLiveData.setValue(false)
                    }
                }
        } else {
            loginFirebaseMutableLiveData.setValue(null)
        }
        return loginFirebaseMutableLiveData
    }

    fun registerFirebase(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean
    ): MutableLiveData<RegistrationState>? {
        val registerFirebaseMutableLiveData: MutableLiveData<RegistrationState> =
            MutableLiveData<RegistrationState>()
        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (isEmailValid(email)) {
                if (acceptedTerms) {
                    if (password == confirmPassword) {
                        firebaseReference.child(ACCOUNTS)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(p0: DataSnapshot) {
                                    var usernameExists = false
                                    p0.children.forEach {
                                        if (it.value?.equals(username)!!) {
                                            registerFirebaseMutableLiveData.value =
                                                RegistrationState.USERNAME_EXISTS
                                            usernameExists = true
                                        }
                                    }
                                    if (!usernameExists) {
                                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    firebaseReference.child(ACCOUNTS)
                                                        .child(firebaseAuth.currentUser?.uid!!)
                                                        .setValue(username)
                                                    firebaseReference.child(USERS)
                                                        .child(username)
                                                        .setValue(0)
                                                    registerFirebaseMutableLiveData.setValue(
                                                        RegistrationState.SUCCESSFUL
                                                    )
                                                } else {
                                                    Log.d(
                                                        TAG,
                                                        "Error registering: " + task.exception
                                                    )
                                                    registerFirebaseMutableLiveData.setValue(
                                                        RegistrationState.UNSUCCESSFUL
                                                    )
                                                }
                                            }
                                    } else {
                                        registerFirebaseMutableLiveData.value =
                                            RegistrationState.USERNAME_EXISTS
                                    }
                                }

                                override fun onCancelled(p0: DatabaseError) {
                                    Log.d(TAG, "username onCancelled: " + p0.message)
                                    registerFirebaseMutableLiveData.value =
                                        RegistrationState.UNSUCCESSFUL
                                }
                            })
                    } else {
                        registerFirebaseMutableLiveData.setValue(RegistrationState.PASSWORDS_NOT_EQUAL)
                    }
                } else {
                    registerFirebaseMutableLiveData.setValue(RegistrationState.NOT_ACCEPTED_TERMS)
                }
            } else {
                registerFirebaseMutableLiveData.setValue(RegistrationState.NOT_VALID_EMAIL)
            }
        } else {
            registerFirebaseMutableLiveData.setValue(RegistrationState.CREDENTIALS_NOT_OK)
        }
        return registerFirebaseMutableLiveData
    }

    fun changePasswordFirebase(
        oldPassword: String,
        newPassword: String
    ): MutableLiveData<Boolean>? {
        val changePasswordFirebaseMutableLiveData: MutableLiveData<Boolean> =
            MutableLiveData<Boolean>()
        val user = firebaseAuth.currentUser
        val email: String = user?.email.toString()
        val credential: AuthCredential = EmailAuthProvider.getCredential(email, oldPassword)
        user?.reauthenticate(credential)?.addOnCompleteListener { task1 ->
            if (task1.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        changePasswordFirebaseMutableLiveData.setValue(true)
                    } else changePasswordFirebaseMutableLiveData.setValue(false)
                }
            } else changePasswordFirebaseMutableLiveData.setValue(false)
        }
        return changePasswordFirebaseMutableLiveData
    }

    fun deleteAccountFirebase(password: String): MutableLiveData<Boolean> {
        val deleteAccountFirebaseMutableLiveData: MutableLiveData<Boolean> =
            MutableLiveData<Boolean>()
        val user = FirebaseAuth.getInstance().currentUser
        val email: String = user?.email.toString()
        val credential: AuthCredential = EmailAuthProvider.getCredential(email, password)
        user?.reauthenticate(credential)?.addOnCompleteListener { task1 ->
            if (task1.isSuccessful) {
                val uid = firebaseAuth.currentUser?.uid
                user.delete().addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        deleteAccountFirebaseMutableLiveData.value = true
                        val ref = FirebaseDatabase.getInstance().reference
                        ref.child(ACCOUNTS)
                            .child(uid!!)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.value != null) {
                                        val userName: String = dataSnapshot.value as String
                                        ref.child(USERS).child(userName).removeValue()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.w(
                                        TAG,
                                        "loadUser:onCancelled",
                                        databaseError.toException()
                                    )
                                }
                            })
                        ref.child(ACCOUNTS)
                            .child(uid).removeValue()
                    } else deleteAccountFirebaseMutableLiveData.setValue(false)
                }
            } else deleteAccountFirebaseMutableLiveData.setValue(false)
        }
        return deleteAccountFirebaseMutableLiveData
    }

    companion object {
        private var INSTANCE: AuthenticationRepository? = null
        fun getInstance() = INSTANCE
            ?: AuthenticationRepository().also {
                INSTANCE = it
            }
    }
}