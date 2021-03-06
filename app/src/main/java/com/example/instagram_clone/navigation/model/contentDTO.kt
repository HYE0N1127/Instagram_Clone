package com.example.instagram_clone.navigation.model

data class contentDTO(
    var explain: String? = null,
    var imageUrl: String? = null,
    var uid: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int? = 0,
    var favorites :MutableMap<String, Boolean> = HashMap()) {

    data class comment (var uid: String? = null,
                         var userId: String? = null,
                         var comments : String? = null,
                         var timestamp: Long? = null)
}