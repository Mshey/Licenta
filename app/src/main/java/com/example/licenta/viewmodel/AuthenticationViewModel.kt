package com.example.licenta.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.licenta.model.GenericCallback
import com.example.licenta.model.RegistrationState
import com.example.licenta.model.repository.AuthenticationRepository


class AuthenticationViewModel : ViewModel() {
    private val authenticationRepository = AuthenticationRepository.getInstance()

    var isUserAuthenticatedLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var loginLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var registerLiveData: MutableLiveData<RegistrationState> = MutableLiveData()
    var changePasswordLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var deleteAccountLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun checkIfUserIsAuthenticated() {
        authenticationRepository.checkIfUserIsAuthenticatedInFirebase(object :
            GenericCallback<Boolean> {
            override fun onResponseReady(generic: Boolean) {
                isUserAuthenticatedLiveData.value = generic
            }

        })
    }

    fun login(email: String, password: String) {
        authenticationRepository.loginFirebase(email, password, object : GenericCallback<Boolean?> {
            override fun onResponseReady(generic: Boolean?) {
                loginLiveData.value = generic
            }
        })
    }

    fun register(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean
    ) {
        authenticationRepository.registerFirebase(
            email,
            username,
            password,
            confirmPassword,
            acceptedTerms,
            object : GenericCallback<RegistrationState> {
                override fun onResponseReady(generic: RegistrationState) {
                    registerLiveData.value = generic
                }
            })
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        authenticationRepository
            .changePasswordFirebase(oldPassword, newPassword, object : GenericCallback<Boolean> {
                override fun onResponseReady(generic: Boolean) {
                    changePasswordLiveData.value = generic
                }
            })
    }

    fun deleteAccount(password: String) {
        authenticationRepository.deleteAccountFirebase(password, object : GenericCallback<Boolean> {
            override fun onResponseReady(generic: Boolean) {
                deleteAccountLiveData.value = generic
            }
        })
    }
}