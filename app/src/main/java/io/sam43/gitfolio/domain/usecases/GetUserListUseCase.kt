package io.sam43.gitfolio.domain.usecases

import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.data.helper.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserListUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(query: String? = null): Flow<Result<List<User>, ErrorType>> {
        return if (query.isNullOrBlank()) {
            userRepository.getUsers()
        } else {
            // filter it offline for better performance, still keeping this for future usecases
            userRepository.searchUsers(query)
        }
    }
}