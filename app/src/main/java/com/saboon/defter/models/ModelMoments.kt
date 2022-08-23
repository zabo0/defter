package com.saboon.defter.models

data class ModelMoments(
    val id: String,
    val sender: String,
    val senderUserName: String,
    val date: Long,
    val dateAdded: Long,
    val photoURL: String,
    val resizedPhotoURL: String,
    val text: String
)
