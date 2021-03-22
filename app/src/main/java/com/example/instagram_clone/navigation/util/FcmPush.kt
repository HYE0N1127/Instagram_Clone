package com.example.instagram_clone.navigation.util

import com.example.instagram_clone.navigation.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class FcmPush {
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "AIzaSyDHs1aurNjpfQBqwYYmq-VID8QyEaHKbrU"
    var gson: Gson? = null
    var okHttpClient: OkHttpClient? = null

    companion object {
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid: String, title: String, message: String) {
        //상대방의 uid를 통해 토큰받아오기
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var token = task?.result?.get("pushToken").toString()
                    var pushDTO = PushDTO()

                    pushDTO.to = token
                    pushDTO.notification.title = title
                    pushDTO.notification.body = message

                    var body = RequestBody.create(JSON, gson?.toJson(pushDTO))
                    var request = Request.Builder().addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key=" + serverKey)
                        .url(url)
                        .post(body)
                        .build()

                    //newCall에 Request값을 넣어줘서 구글에 넘겨줌
                    okHttpClient?.newCall(request)?.enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            TODO("Not yet implemented")
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            println(response?.body()?.string())
                        }

                    })
                }
            }
    }
}