package io.sam43.gitfolio.domain.model

data class User(
    val id: Int,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val type: String
)
