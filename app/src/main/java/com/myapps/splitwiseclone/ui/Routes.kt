package com.myapps.splitwiseclone.ui

class Routes {
    companion object {
        val loginScreen = "login"
        val homeScreen = "home"
        val registerScreen = "register"
        val createGroupScreen = "createGroup"
        val selectGroupSelectMembersScreen = "selectUsers"

        val groupMessagesScreen = "groupMessages/{groupId}"
        fun groupMessagesScreen(groupId: String) = "groupMessages/${groupId}"

        val groupEditScreen = "groupEdit/{groupId}"
        fun groupEditScreen(groupId: String) = "groupEdit/${groupId}"


        val createSplitScreen = "createSplit/{groupId}/{amount}"
        fun createSplitScreen(groupId: String, amount : Int) = "createSplit/$groupId/$amount"

    }
}