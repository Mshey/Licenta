package com.example.licenta.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.licenta.model.RegistrationState
import com.example.licenta.model.repository.AuthenticationRepository


class AuthenticationViewModel : ViewModel() {
    var isUserAuthenticatedLiveData: LiveData<Boolean>? = null
    var loginLiveData: LiveData<Boolean>? = null
    var registerLiveData: LiveData<RegistrationState>? = null
    var changePasswordLiveData: LiveData<Boolean>? = null
    var deleteAccountLiveData: LiveData<Boolean>? = null

    fun checkIfUserIsAuthenticated() {
        isUserAuthenticatedLiveData =
            AuthenticationRepository.getInstance().checkIfUserIsAuthenticatedInFirebase()
    }

    fun login(email: String, password: String) {
        loginLiveData = AuthenticationRepository.getInstance().loginFirebase(email, password)
    }

    fun register(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean
    ) {
        registerLiveData = AuthenticationRepository.getInstance()
            .registerFirebase(email, username, password, confirmPassword, acceptedTerms)
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        changePasswordLiveData = AuthenticationRepository.getInstance()
            .changePasswordFirebase(oldPassword, newPassword)
    }

    fun deleteAccount(password: String){
        deleteAccountLiveData = AuthenticationRepository.getInstance().deleteAccountFirebase(password)
    }
}