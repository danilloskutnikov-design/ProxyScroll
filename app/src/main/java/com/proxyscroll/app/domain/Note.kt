package com.proxyscroll.app.domain

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
