package consulting.timhatdiehandandermaus.infrastructure.repository

import com.radcortez.flyway.test.junit.DataSourceInfo
import com.radcortez.flyway.test.junit.DataSourceProvider
import org.eclipse.microprofile.config.ConfigProvider
import org.junit.jupiter.api.extension.ExtensionContext

class QuarkusDataSourceProvider : DataSourceProvider {
    override fun getDatasourceInfo(extensionContext: ExtensionContext?): DataSourceInfo {
        val config = ConfigProvider.getConfig()

        val url = config.getValue("quarkus.datasource.jdbc.url", String::class.java)
        val user = config.getValue("quarkus.datasource.username", String::class.java)
        val password = config.getValue("quarkus.datasource.password", String::class.java)

        return DataSourceInfo.config(url, user, password)
    }
}
