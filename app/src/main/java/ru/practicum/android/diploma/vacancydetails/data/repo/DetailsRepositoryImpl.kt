package ru.practicum.android.diploma.vacancydetails.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.common.data.NetworkClient
import ru.practicum.android.diploma.common.data.Resource
import ru.practicum.android.diploma.common.domain.EmployerInfo
import ru.practicum.android.diploma.common.domain.NameInfo
import ru.practicum.android.diploma.common.domain.SalaryInfo
import ru.practicum.android.diploma.vacancydetails.data.dto.AddressDto
import ru.practicum.android.diploma.vacancydetails.data.dto.ContactsDto
import ru.practicum.android.diploma.vacancydetails.data.dto.NameInfoDto
import ru.practicum.android.diploma.vacancydetails.data.dto.SalaryDto
import ru.practicum.android.diploma.vacancydetails.data.dto.VacancyDetailsRequest
import ru.practicum.android.diploma.vacancydetails.data.dto.VacancyDetailsResponse
import ru.practicum.android.diploma.vacancydetails.domain.api.DetailsRepository
import ru.practicum.android.diploma.vacancydetails.domain.models.Address
import ru.practicum.android.diploma.vacancydetails.domain.models.Contacts
import ru.practicum.android.diploma.vacancydetails.domain.models.Details
import ru.practicum.android.diploma.vacancydetails.domain.models.KeySkill
import ru.practicum.android.diploma.vacancydetails.domain.models.Phone
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails

class DetailsRepositoryImpl(private val networkClient: NetworkClient) : DetailsRepository {
    override fun getVacancyDetails(vacancyId: String): Flow<Resource<VacancyDetails?>> = flow {
        when (val response = networkClient.doRequest(VacancyDetailsRequest(vacancyId))) {
            is VacancyDetailsResponse -> {
                emit(transformVacancyDetails(response))
            }

            else -> {
                emit(Resource.Error(response.errorType))
            }
        }
    }

    private fun transformVacancyDetails(it: VacancyDetailsResponse): Resource<VacancyDetails?> {
        return Resource.Success(
            VacancyDetails(
                it.id,
                it.name,
                isFavorite = false,
                employerInfo = EmployerInfo(
                    areaName = it.area.name,
                    employerName = it.employer?.name ?: "",
                    employerLogoUrl = it.employer?.logoUrls?.logo240
                ),
                salaryInfo = if (it.salary != null) {
                    transformSalaryInfo(it.salary)
                } else {
                    null
                },
                details = Details(
                    address = transformAddress(it.address),
                    experience = if (it.experience != null) {
                        transformExperience(it.experience)
                    } else {
                        null
                    },
                    employment = NameInfo(id = it.employment?.id ?: "", name = it.employment?.name ?: ""),
                    schedule = NameInfo(id = it.schedule?.id ?: "", name = it.schedule?.name ?: ""),
                    description = it.description,
                    keySkill = it.keySkill?.map {
                        KeySkill(it.name)
                    },
                    contacts = if (it.contacts != null) {
                        transformContacts(it.contacts)
                    } else {
                        Contacts("", "", mutableListOf())
                    },
                    hhVacancyLink = it.hhVacancyLink
                )
            )
        )
    }

    private fun transformSalaryInfo(salary: SalaryDto): SalaryInfo? {
        return salary.let {
            SalaryInfo(
                salaryFrom = salary.from,
                salaryTo = salary.to,
                salaryCurrency = salary.currency
            )
        }
    }

    private fun transformContacts(contacts: ContactsDto): Contacts {
        return contacts.let { contactsDto ->
            Contacts(
                email = contacts.email,
                name = contacts.name,
                phone = contacts.phones?.map { phoneDto ->
                    Phone(
                        city = phoneDto.city,
                        comment = phoneDto.comment,
                        country = phoneDto.country,
                        formatted = phoneDto.formatted,
                        number = phoneDto.number
                    )
                }
            )
        } ?: Contacts("", "", null) // Возвращает пустой, при отсутствии данных
    }

    private fun transformAddress(addressDto: AddressDto?): Address {
        return addressDto?.let {
            Address(
                city = it.city,
                building = it.building,
                street = it.street,
                description = it.description
            )
        } ?: Address(
            city = "",
            building = "",
            street = "",
            description = ""
        )
    }

    private fun transformExperience(experience: NameInfoDto): NameInfo {
        return experience.let {
            NameInfo(
                id = it.id ?: "",
                name = it.name
            )
        } ?: NameInfo(
            id = "",
            name = ""
        )
    }
}
