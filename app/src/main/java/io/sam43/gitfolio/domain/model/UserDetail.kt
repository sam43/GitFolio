package io.sam43.gitfolio.domain.model

data class UserDetail(
    val id: Int,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val name: String?,
    val company: String?,
    val blog: String?,
    val location: String?,
    val email: String?,
    val bio: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val createdAt: String,
    val updatedAt: String
)
