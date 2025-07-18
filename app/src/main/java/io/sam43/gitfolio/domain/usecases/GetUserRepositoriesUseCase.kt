package io.sam43.gitfolio.domain.usecases

import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserRepositoriesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): Flow<Result<List<Repo>>> {
        return userRepository.getUserRepositories(username).map { result ->
            when (result) {
                is Result.Success -> {
                    // Filter out forked repositories
                    val nonForkedRepos = result.data.filter { !it.fork }
                    Result.Success(nonForkedRepos)
                }
                is Result.Error -> result
                is Result.Loading -> result
            }
        }
    }
}
