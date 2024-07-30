package com.myapps.splitwiseclone.models

data class ExpenseSplit(
    var createdBy : UserAccount = UserAccount(),
    var splitDetails : ArrayList<SplitDetail> = ArrayList(),
    var createdAt : Long = 0L,
    var message: String ="",
    var totalAmount : Long = 0L
)