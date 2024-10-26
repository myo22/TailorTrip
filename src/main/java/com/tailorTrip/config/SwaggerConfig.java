package com.tailorTrip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

// @RestController 어노테이션이 있는 컨트롤러들에 대해서 API문서를 생성하도록 작성합니다.
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)
                .useDefaultResponseMessages(false)
                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.example.board.controller")) // 이 설정은 특정 패키지 내에 있는 모든 클래스의 메서드를 문서화 대상으로 지정합니다.
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class)) // 이 설정은 특정 애너테이션이 붙은 클래스의 메서드만 문서화 대상으로 지정합니다.
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(List.of(apiKey())) // 추가된 부분
                .securityContexts(List.of(securityContext())) // 추가된 부분
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("tailorTrip Swagger")
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Bearer Token", "header");
    }

    private SecurityContext securityContext(){
        return SecurityContext.builder().securityReferences(defaultAuth())
                .operationSelector(selector -> selector.requestMappingPattern().startsWith("/api/")).build();
    }

    private List<SecurityReference> defaultAuth(){
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "global access");
        return List.of(new SecurityReference("Authorization", new AuthorizationScope[] {authorizationScope}));
    }
}
