package android.example.threadsverificationtask

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

class Thread3(val count: Int) : Thread() {

    companion object {
        const val TAG = "Thread3"
    }

    private var countDownLatch = CountDownLatch(count)
    private val lock = ReentrantLock()

    private val phoneDataList = ArrayList<String>()

    fun addPhoneData(data: String) {
        synchronized(this) {
            phoneDataList.add(data)
            countDownLatch.countDown()
        }
    }

    override fun run() {
        while (true) {
            if(!Thread.currentThread().isInterrupted) {
                try {
                    countDownLatch.await()
                    lock.lock()
                    val listToSend = phoneDataList.clone()
                    phoneDataList.clear()
                    countDownLatch = CountDownLatch(count)
                    lock.unlock()
                    sendOverHttp(listToSend as ArrayList<String>)
                } catch (e: InterruptedException) {
                    Log.e(TAG, e.message.toString())
                }
            } else {
                break
            }
        }
    }

    private fun sendOverHttp(list: ArrayList<String>) {
        val jsonObject = JSONObject()
        jsonObject.put("phoneDataList", list)
        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            val response = Api.retrofitService.postData(requestBody)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.d(TAG, "success")
                } else {
                    Log.e(TAG, response.code().toString())
                }
            }
        }
    }
}