package com.saboon.defter.models

data class ModelMoments(
    var id: String,
    val senderUID: String,
    val date: Long,
    val dateAdded: Long,
    val photoURL: String,
    val resizedPhotoURL: String,
    val text: String
)
