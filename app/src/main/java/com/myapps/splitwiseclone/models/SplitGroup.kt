package com.myapps.splitwiseclone.models;

data class SplitGroup(
    var createdBy: String = "",
    var groupName : String = "",
    var groupMembers: ArrayList<String> = ArrayList(),
    var expenseSplits: ArrayList<ExpenseSplit> = ArrayList()
)
