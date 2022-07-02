package com.example.demo.application


import com.example.demo.application.security.AudienceValidator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers

@Configuration
class SecurityConfig {

//    @Bean
//    fun userDetailsService(): ReactiveUserDetailsService {
//        val user = User.withDefaultPasswordEncoder()
//        val users = listOf<UserDetails>(
//            user.username("user").password("password").roles("USER").build(),
//            user.username("admin").password("password").roles("USER", "ADMIN").build(),
//        )
//        return MapReactiveUserDetailsService(users)
//    }

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity, reactiveJwtDecoder: ReactiveJwtDecoder): SecurityWebFilterChain =
        http {
            csrf { disable() }
            httpBasic { disable() }
            formLogin { disable() }
            logout { disable() }

            // enable OAuth2 resource server support
            oauth2ResourceServer { jwt { jwtDecoder = reactiveJwtDecoder } }
            authorizeExchange {
                authorize(pathMatchers(GET, "/me"), authenticated)
                authorize(pathMatchers(GET, "/posts/**"), permitAll)
                authorize(pathMatchers(POST, "/posts/**"), hasAuthority("SCOPE_write:posts"))
                authorize(pathMatchers(PUT, "/posts/**"), hasAuthority("SCOPE_write:posts"))
                authorize(pathMatchers(DELETE, "/posts/**"), hasAuthority("SCOPE_delete:posts"))
                authorize(anyExchange, permitAll)
            }
        }

    @Bean
    fun reactiveJwtDecoder(
        properties: OAuth2ResourceServerProperties,
        @Value("\${auth0.audience}") audience: String,
    ): ReactiveJwtDecoder {
        val issuerUri = properties.jwt.issuerUri
        val jwtDecoder = ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri) as NimbusReactiveJwtDecoder

        val audienceValidator: OAuth2TokenValidator<Jwt> = AudienceValidator(audience)
        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(issuerUri)
        val withAudience: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)

        jwtDecoder.setJwtValidator(withAudience)
        return jwtDecoder
    }

    companion object {
        private val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    }
}
