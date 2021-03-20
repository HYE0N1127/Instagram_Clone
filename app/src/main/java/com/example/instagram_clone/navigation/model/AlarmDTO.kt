package com.example.instagram_clone.navigation.model

import java.security.MessageDigest
import java.security.Timestamp

data class AlarmDTO(
    var destinationUid: String? = null,
    var userId: String? = null,
    var uid: String? = null,
    var kind: Int? = null,
    var message: String? = null,
    var timestamp: Long? = null
)
