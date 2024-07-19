package ru.practicum.android.diploma.filters.chooseindustry.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.common.data.NetworkClient
import ru.practicum.android.diploma.common.data.Resource
import ru.practicum.android.diploma.filters.chooseindustry.data.dto.IndustrtesRequest
import ru.practicum.android.diploma.filters.chooseindustry.data.dto.IndustryDto
import ru.practicum.android.diploma.filters.chooseindustry.data.dto.IndustryResponse
import ru.practicum.android.diploma.filters.chooseindustry.domain.interfaces.IndustryRepository
import ru.practicum.android.diploma.filters.chooseindustry.domain.model.IndustriesModel
import ru.practicum.android.diploma.filters.chooseindustry.domain.model.IndustriesResult

class IndustryRepositoryImpl(private val networkClient: NetworkClient): IndustryRepository {
    override fun getIndustries(): Flow<Resource<IndustriesResult>> = flow {
        when (val response = networkClient.doRequest(IndustrtesRequest())) {
            is IndustryResponse -> {
                emit(
                    Resource
                        .Success(
                            IndustriesResult(
                                industries = response.industries.map(::ConvertIndustry)
                            )
                        )
                )

            }
            else -> {
                emit(Resource.Error(response.errorType))
            }
        }
    }

    private fun ConvertIndustry(it: IndustryDto): IndustriesModel =
        IndustriesModel(
            it.id,
            it.name,
        )
}
