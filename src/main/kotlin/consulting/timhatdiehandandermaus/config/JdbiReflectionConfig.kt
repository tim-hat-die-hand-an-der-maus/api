package consulting.timhatdiehandandermaus.config

import io.quarkus.runtime.annotations.RegisterForReflection
import org.jdbi.v3.core.kotlin.KotlinMapperFactory

@RegisterForReflection(targets = [KotlinMapperFactory::class])
class JdbiReflectionConfig
