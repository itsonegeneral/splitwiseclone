package com.myapps.splitwiseclone.models;

data class SplitGroup(
    var groupId : String ="",
    var createdBy: String = "",
    var groupName : String = "",
    var groupMembers: ArrayList<String> = ArrayList()
)
