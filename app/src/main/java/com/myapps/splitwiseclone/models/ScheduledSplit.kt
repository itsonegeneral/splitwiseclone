package com.myapps.splitwiseclone.models

data class ScheduledSplit(
    var scheduledSplitId: String = "",
    var splitMode: String = "",
    var expenseSplit: ExpenseSplit = ExpenseSplit(),
    var createdAt: Long = 0L,
    var triggerTime : Long = 0L
) {
    override fun toString(): String {
        return "[scheduledSplitId:$scheduledSplitId,splitMode:$splitMode]"
    }
}