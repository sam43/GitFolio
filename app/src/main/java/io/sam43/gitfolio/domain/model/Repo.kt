package io.sam43.gitfolio.domain.model

data class Repo(
    val id: Int,
    val name: String,
    val fullName: String,
    val description: String?,
    val htmlUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val openIssuesCount: Int,
    val fork: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val pushedAt: String?
)
