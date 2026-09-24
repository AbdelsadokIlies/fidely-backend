package com.fidely.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**

 * Propriétés de configuration de l'URL de base de l'API Fidely.
 */
@ConfigurationProperties(prefix = "fidely.api-url")
public class ApiUrlProperties {

    private String url;

    /**

     * Retourne l'URL de base de l'API.
     *
     * @return URL de base de l'API
     */
    public String getUrl() {
        return url;
    }

    /**

     * Définit l'URL de base de l'API.
     *
     * @param url URL de base de l'API
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
