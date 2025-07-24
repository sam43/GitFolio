package io.sam43.gitfolio.domain.usecases

import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.data.helper.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(username: String): Flow<Result<UserDetail, ErrorType>> = userRepository.getUserDetails(username)
}
