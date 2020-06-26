package com.example.licenta

class NavEvent(
    val destination: Destination
) {
    enum class Destination {
        REGISTRATION, LOGIN, MYACCOUNT, TERMS, CHANGEPASSWORD, FUTURE, PAST, CREATE, DETAILS, MAP
    }
}