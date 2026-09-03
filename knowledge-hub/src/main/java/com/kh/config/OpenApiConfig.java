package com.kh.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档元数据配置。
 * 访问入口（context-path 为 /api）：
 *   Knife4j 增强文档: http://localhost:8080/api/doc.html
 *   原生 Swagger UI:  http://localhost:8080/api/swagger-ui.html
 *   OpenAPI JSON:     http://localhost:8080/api/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    /** 全局请求头名称：阶段① 实现 JWT 后，文档右上角「Authorize」填入 token 即可调试 */
    public static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Knowledge Hub API")
                        .description("AI 原生知识文件管理系统 —— 阶段① 文件管理模块接口文档")
                        .version("v0.1.0"))
                // HTTP Bearer 方案：文档 Authorize 中粘贴 accessToken 即可，请求头自动携带 "Bearer " 前缀。
                // 注意不能用 APIKEY 方案——它把值原样放入请求头（不带 Bearer），会被 JWT 过滤器当成匿名导致 1010
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录接口返回的 accessToken，无需手动加 Bearer 前缀")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
