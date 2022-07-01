package com.example.demo.application

import org.hibernate.validator.internal.engine.DefaultClockProvider
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.validation.MessageInterpolatorFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Role
import org.springframework.core.KotlinReflectionParameterNameDiscoverer
import org.springframework.core.LocalVariableTableParameterNameDiscoverer
import org.springframework.core.ParameterNameDiscoverer
import org.springframework.core.PrioritizedParameterNameDiscoverer
import org.springframework.core.StandardReflectionParameterNameDiscoverer
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import javax.validation.ClockProvider
import javax.validation.ParameterNameProvider
import kotlin.reflect.jvm.kotlinFunction

// workaround for validation on Kotlin Coroutines controller.
// see: https://github.com/spring-projects/spring-framework/issues/23499#issuecomment-900369875
// related issues see: https://github.com/spring-projects/spring-framework/issues/23499
// and https://hibernate.atlassian.net/browse/HV-1638
@Configuration
class ValidationConfig {
    @Primary
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun defaultValidator(): LocalValidatorFactoryBean {
        val factoryBean = KotlinCoroutinesLocalValidatorFactoryBean()
        factoryBean.messageInterpolator = MessageInterpolatorFactory().getObject()
        return factoryBean
    }
}

class KotlinCoroutinesLocalValidatorFactoryBean : LocalValidatorFactoryBean() {
    override fun getClockProvider(): ClockProvider = DefaultClockProvider.INSTANCE

    override fun postProcessConfiguration(configuration: javax.validation.Configuration<*>) {
        super.postProcessConfiguration(configuration)

        val discoverer = PrioritizedParameterNameDiscoverer()
        discoverer.addDiscoverer(SuspendAwareKotlinParameterNameDiscoverer())
        discoverer.addDiscoverer(StandardReflectionParameterNameDiscoverer())
        discoverer.addDiscoverer(LocalVariableTableParameterNameDiscoverer())

        val defaultProvider = configuration.defaultParameterNameProvider
        configuration.parameterNameProvider(object : ParameterNameProvider {
            override fun getParameterNames(constructor: Constructor<*>): List<String> {
                val paramNames: Array<String>? = discoverer.getParameterNames(constructor)
                return paramNames?.toList() ?: defaultProvider.getParameterNames(constructor)
            }

            override fun getParameterNames(method: Method): List<String> {
                val paramNames: Array<String>? = discoverer.getParameterNames(method)
                return paramNames?.toList() ?: defaultProvider.getParameterNames(method)
            }
        })
    }
}

class SuspendAwareKotlinParameterNameDiscoverer : ParameterNameDiscoverer {

    private val defaultProvider = KotlinReflectionParameterNameDiscoverer()

    override fun getParameterNames(constructor: Constructor<*>): Array<String>? =
        defaultProvider.getParameterNames(constructor)

    override fun getParameterNames(method: Method): Array<String>? {
        val defaultNames = defaultProvider.getParameterNames(method) ?: return null
        val function = method.kotlinFunction
        return if (function != null && function.isSuspend) {
            defaultNames + ""
        } else defaultNames
    }
}
