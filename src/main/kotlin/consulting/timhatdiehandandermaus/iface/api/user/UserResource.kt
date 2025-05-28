package consulting.timhatdiehandandermaus.iface.api.user

import consulting.timhatdiehandandermaus.application.exception.DuplicateException
import consulting.timhatdiehandandermaus.application.exception.NotFoundException
import consulting.timhatdiehandandermaus.application.usecase.GetUserById
import consulting.timhatdiehandandermaus.application.usecase.GetUserByTelegramId
import consulting.timhatdiehandandermaus.application.usecase.UpdateTelegramUser
import consulting.timhatdiehandandermaus.iface.api.model.TelegramUserRequest
import consulting.timhatdiehandandermaus.iface.api.model.UserRequestMapper
import consulting.timhatdiehandandermaus.iface.api.model.UserResponse
import consulting.timhatdiehandandermaus.iface.api.model.UserResponseMapper
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.WebApplicationException
import java.util.UUID
import jakarta.ws.rs.NotFoundException as JakartaNotFoundException

@Path("/user")
@RequestScoped
@Authenticated
class UserResource
    @Inject
    constructor(
        private val requestMapper: UserRequestMapper,
        private val responseMapper: UserResponseMapper,
        private val getUserByTelegramId: GetUserByTelegramId,
        private val getUserById: GetUserById,
        private val updateTelegramUser: UpdateTelegramUser,
    ) {
        @PUT
        @Path("/telegram")
        fun putTelegramUser(
            @QueryParam("existingId")
            existingId: UUID?,
            body: TelegramUserRequest,
        ): UserResponse {
            val telegramUser = requestMapper.toModel(body)
            val canonicalUser =
                try {
                    updateTelegramUser(
                        existingUserId = existingId,
                        telegramUser = telegramUser,
                    )
                } catch (e: NotFoundException) {
                    throw JakartaNotFoundException(e)
                } catch (e: DuplicateException) {
                    throw WebApplicationException(e, 409)
                }

            return responseMapper.toDto(canonicalUser)
        }

        @GET
        @Path("/telegramId/{id}")
        fun getByTelegramId(
            @PathParam("id") telegramId: Long,
        ): UserResponse {
            val user = getUserByTelegramId(telegramId)
            if (user == null) {
                throw JakartaNotFoundException()
            }
            return responseMapper.toDto(user)
        }

        @GET
        @Path("/{id}")
        fun getById(
            @PathParam("id") id: UUID,
        ): UserResponse {
            val user = getUserById(id)
            if (user == null) {
                throw JakartaNotFoundException()
            }
            return responseMapper.toDto(user)
        }
    }
