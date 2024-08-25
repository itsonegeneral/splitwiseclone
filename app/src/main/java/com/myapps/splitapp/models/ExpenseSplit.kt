package com.myapps.splitapp.models

data class ExpenseSplit(
    var expenseSplitId : String = "",
    var createdBy : UserAccount = UserAccount(),
    var splitDetails : ArrayList<SplitDetail> = ArrayList(),
    var createdAt : Long = 0L,
    var message: String ="",
    var totalAmount : Double = 0.0,
    var scheduleId : String ="",
    var currency : String = "$"
)