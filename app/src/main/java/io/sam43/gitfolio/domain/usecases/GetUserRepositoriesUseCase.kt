package io.sam43.gitfolio.domain.usecases

import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.Result
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserRepositoriesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(username: String): Flow<Result<List<Repo>, ErrorType>> =
        userRepository.getUserRepositories(username)
}
