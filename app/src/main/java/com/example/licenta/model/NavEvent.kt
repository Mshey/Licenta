package com.example.licenta.model

class NavEvent(
    val destination: Destination
) {
    enum class Destination {
        REGISTRATION, LOGIN, MYACCOUNT, TERMS, CHANGEPASSWORD, FUTURE, CREATE, DETAILS, MAP
    }
}