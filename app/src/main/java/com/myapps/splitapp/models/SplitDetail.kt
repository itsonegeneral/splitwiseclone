package com.myapps.splitapp.models

data class SplitDetail(
    var userAccount: UserAccount = UserAccount(),
    var amount: Double = 0.0,
    var isPaid: Boolean = false
)
