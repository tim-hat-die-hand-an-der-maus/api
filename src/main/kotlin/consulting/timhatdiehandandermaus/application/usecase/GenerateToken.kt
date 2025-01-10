package consulting.timhatdiehandandermaus.application.usecase

import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.RequestScoped

@RequestScoped
class GenerateToken {
    operator fun invoke(serviceName: String): String {
        require(serviceName.isNotBlank())
        return Jwt
            .claims()
            .audience("service")
            .subject(serviceName)
            .sign()
    }
}
