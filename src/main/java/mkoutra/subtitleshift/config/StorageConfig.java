package mkoutra.subtitleshift.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    /**
     * Creates a StorageProperties bean and binds properties
     * with the "storage" prefix from application.properties.
     *
     * @return A configured StorageProperties instance.
     */
    @Bean
    @ConfigurationProperties(prefix = "storage")
    public StorageProperties storageProperties() {
        return new StorageProperties();
    }
}
