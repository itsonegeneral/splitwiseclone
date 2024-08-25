package com.myapps.splitapp.server

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.myapps.splitapp.DatabaseKeys
import com.myapps.splitapp.models.ExpenseSplit
import com.myapps.splitapp.models.ScheduledSplit

class ServerMocker {

    companion object {
        private const val TAG = "ServerMocker"
        private const val daysInMillis = 86400000
        fun refreshSchedules() {
            val needToMoveSplits = ArrayList<ExpenseSplit>()
            Log.d(TAG, "refreshSchedules: 1")
            Firebase.database.reference.child(DatabaseKeys.schedules).get()
                .addOnSuccessListener { groupIds->
                    groupIds.children.forEach { scheduleList ->
                        val schedules = scheduleList.children.mapNotNull { it.getValue(ScheduledSplit::class.java) }
                        Log.d(TAG, "refreshSchedules: Got ${schedules.size} schedules in ${scheduleList.key}")


                        schedules.forEach { schedule ->
                            Log.d(TAG, "refreshSchedules: Checking ${schedule.scheduledSplitId}")
                            Log.d(TAG, "refreshSchedules: current time ${System.currentTimeMillis()} ${schedule.triggerTime > System.currentTimeMillis()}")
                            if (schedule.triggerTime < System.currentTimeMillis()) {
                                schedule.expenseSplit.scheduleId = schedule.scheduledSplitId
                                moveSplitToGroup(scheduleList.key!!, schedule.expenseSplit, schedule)
                                Log.d(TAG, "moveSplitToGroup: Moving ${schedule.scheduledSplitId}")
                            } else {
                                Log.d(
                                    TAG,
                                    "refreshSchedules: No need to move ${schedule.scheduledSplitId}"
                                )
                            }
                        }
                    }
                }

        }

        private fun moveSplitToGroup(groupId: String, expenseSplit: ExpenseSplit, scheduledSplit: ScheduledSplit) {

            val splitRef = Firebase.database.reference.child(DatabaseKeys.splits).child(groupId).push()
            expenseSplit.expenseSplitId = splitRef.key!!
            splitRef.setValue(expenseSplit).addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d(TAG,"Moved split ${expenseSplit.expenseSplitId}")
                    if(scheduledSplit.timesToExecute == 1){
                        Firebase.database.reference.child(DatabaseKeys.schedules).child(groupId).child(scheduledSplit.scheduledSplitId).removeValue()
                    }else{
                        scheduledSplit.timesToExecute -=1
                        scheduledSplit.triggerTime = scheduledSplit.triggerTime + (scheduledSplit.triggerInterval * daysInMillis)
                        Firebase.database.reference.child(DatabaseKeys.schedules).child(groupId).child(scheduledSplit.scheduledSplitId).setValue(scheduledSplit)
                    }
                }else{
                    Log.d(TAG,"unable to move split ${expenseSplit.expenseSplitId}")
                }
            }
        }

        fun addDaysToCurrentTimeInMillis(days : Int): Long{
            return System.currentTimeMillis() + (days * daysInMillis)
        }

    }
}