package com.fidely.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Point d'entrée de l'application backend Fidely.
 *
 * <p>Cette classe démarre l'application Spring Boot et initialise
 * l'ensemble du contexte Spring.</p>
 *
 * <p>Les classes annotées avec {@link ConfigurationPropertiesScan}
 * sont automatiquement détectées et enregistrées comme beans Spring.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class FidelyBackendApplication {

    /**
     * Démarre l'application Fidely.
     *
     * @param args arguments passés au démarrage de l'application
     */
    public static void main(String[] args) {
        SpringApplication.run(FidelyBackendApplication.class, args);
    }
}