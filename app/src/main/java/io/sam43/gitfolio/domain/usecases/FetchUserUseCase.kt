package io.sam43.gitfolio.domain.usecases

import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Flow<Result<List<User>>> {
        return userRepository.searchUsers(query)
    }
}
