package com.myapps.splitwiseclone.ui

class Routes {
    companion object {
        val loginScreen = "login"
        val homeScreen = "home"
        val registerScreen = "register"
        val createGroupScreen = "createGroup"
        val createGroupSelectMembersScreen = "createGroupSelectMembers/{groupName}"
        fun createGroupSelectMembersScreen(groupName: String) = "createGroupSelectMembers/${groupName}"

        val groupMessagesScreen = "groupMessages/{groupId}"
        fun groupMessagesScreen(groupId: String) = "groupMessages/${groupId}"

        val createSplitScreen = "createSplit/{groupId}/{amount}"
        fun createSplitScreen(groupId: String, amount : Int) = "createSplit/$groupId/$amount"

    }
}