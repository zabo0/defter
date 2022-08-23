package com.saboon.defter.models

data class ModelHomeSection(
    val date: String,
    val sendersPp: List<String>,
    val moments: List<ModelMoments>
)
