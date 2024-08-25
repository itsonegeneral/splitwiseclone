package com.myapps.splitapp.ui

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

        val groupSchedulesScreen = "groupSchedules/{groupId}"
        fun groupSchedulesScreen(groupId: String) = "groupSchedules/${groupId}"


        val createSplitScreen = "createSplit/{groupId}/{amount}"
        fun createSplitScreen(groupId: String, amount: Int) = "createSplit/$groupId/$amount"


        val viewSplitScreen = "viewSplit/{splitId}/{groupId}"
        fun viewSplitScreen(splitId: String, groupId: String) = "viewSplit/$splitId/$groupId"

        val paymentSuccessScreen = "paymentSuccessScreen/{amount}/{paidTo}/{message}"
        fun paymentSuccessScreen(amount: Int, paidTo: String, message: String) =
            "paymentSuccessScreen/${amount}/${paidTo}/${message}"

    }
}