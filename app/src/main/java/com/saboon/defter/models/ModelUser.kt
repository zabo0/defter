package com.saboon.defter.models

data class ModelUser(
    val userName: String,
    val userEmail: String,
    val profilePhotoURL: String,
    val birthday: Long,
    val moments: Int
)