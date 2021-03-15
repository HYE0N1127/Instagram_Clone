package com.example.instagram_clone.navigation.model

data class contentDTO(
    var explain: String? = null,
    var imageUrl: String? = null,
    var uid: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var favotireCount: Int? = 0,
    var favorites :Map<String, Boolean> = HashMap()) {

    data class comments (var uid: String? = null,
                         var userId: String? = null,
                         var comments : String? = null,
                         var timestamp: Long? = null)
}