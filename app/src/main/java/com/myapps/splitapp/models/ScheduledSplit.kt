package com.myapps.splitapp.models

import com.myapps.splitapp.constants.SplitModes

data class ScheduledSplit(
    var scheduledSplitId: String = "",
    var splitMode: String = SplitModes.Scheduled,
    var expenseSplit: ExpenseSplit = ExpenseSplit(),
    var createdAt: Long = 0L,
    var triggerTime : Long = 0L,
    var timesToExecute : Int = 1,
    var triggerInterval : Int = 0,
) {
    override fun toString(): String {
        return "[scheduledSplitId:$scheduledSplitId,splitMode:$splitMode]"
    }
}