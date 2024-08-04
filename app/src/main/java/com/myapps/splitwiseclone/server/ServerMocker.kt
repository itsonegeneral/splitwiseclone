package com.myapps.splitwiseclone.server

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.myapps.splitwiseclone.DatabaseKeys
import com.myapps.splitwiseclone.models.ExpenseSplit
import com.myapps.splitwiseclone.models.ScheduledSplit
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class ServerMocker {

    companion object {
        private const val TAG = "ServerMocker"
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
                                moveSplitToGroup(scheduleList.key!!, schedule.expenseSplit, schedule.scheduledSplitId)
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

        private fun moveSplitToGroup(groupId: String, expenseSplit: ExpenseSplit, scheduleId: String) {

            val splitRef = Firebase.database.reference.child(DatabaseKeys.splits).child(groupId).push()
            expenseSplit.expenseSplitId = splitRef.key!!
            splitRef.setValue(expenseSplit).addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d(TAG,"Moved split ${expenseSplit.expenseSplitId}")
                    Firebase.database.reference.child(DatabaseKeys.schedules).child(groupId).child(scheduleId).removeValue()
                }else{
                    Log.d(TAG,"unable to move split ${expenseSplit.expenseSplitId}")
                }
            }
        }
    }
}