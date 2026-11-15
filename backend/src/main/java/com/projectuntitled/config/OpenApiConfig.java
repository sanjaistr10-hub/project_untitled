package com.projectuntitled.config;

import com.projectuntitled.auth.web.CsrfHeaderFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import java.util.Set;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at /swagger-ui.html, raw spec at /v3/api-docs. */
@Configuration
public class OpenApiConfig {

    private static final Set<PathItem.HttpMethod> UNSAFE =
            Set.of(PathItem.HttpMethod.POST, PathItem.HttpMethod.PUT, PathItem.HttpMethod.PATCH, PathItem.HttpMethod.DELETE);

    @Bean
    public OpenAPI openApi(AppProperties properties) {
        return new OpenAPI().info(new Info()
                .title(properties.brandName() + " API")
                .version("v1")
                .description("REST API for creators (studio) and members (white-label community sites). "
                        + "Sign in with the otp/start + otp/verify endpoints; tokens are set as httpOnly cookies."));
    }

    /** Pre-fills the CSRF header on write operations so "Try it out" works in Swagger UI. */
    @Bean
    public GlobalOpenApiCustomizer csrfHeaderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(item -> item.readOperationsMap().forEach((method, operation) -> {
                if (UNSAFE.contains(method)) {
                    operation.addParametersItem(new HeaderParameter()
                            .name(CsrfHeaderFilter.HEADER)
                            .required(true)
                            .description("CSRF guard for cookie-authenticated writes. Any non-empty value.")
                            .schema(new StringSchema()._default("fetch")));
                }
            }));
        };
    }
}
