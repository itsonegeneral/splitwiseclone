package com.myapps.splitapp.ui.screens.home.groups.split.helper

import androidx.compose.runtime.MutableState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.myapps.splitapp.models.SplitDetail
import com.myapps.splitapp.models.UserAccount


class SplitsHelper {
    companion object {
        fun calculateSplitDetailsForMembers(
            groupMembers: List<UserAccount>,
            splitValues: MutableState<MutableMap<String, Int>>
        ): ArrayList<SplitDetail> {
            val splitDetails = ArrayList<SplitDetail>()

            groupMembers.forEach { user ->
                if (splitValues.value.containsKey(user.uid)) {
                    val splitDetail = SplitDetail()
                    splitDetail.amount = splitValues.value[user.uid]!!.toDouble()
                    splitDetail.isPaid = false
                    splitDetail.userAccount = user

                    //If the user is the split owner, mark as paid
                    if (user.uid == Firebase.auth.uid) {
                        splitDetail.isPaid = true
                    }
                    splitDetails.add(splitDetail)
                }

            }
            return splitDetails;
        }
    }
}