package org.quizbe.config

import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.CookieLocaleResolver
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import java.util.*

@Configuration
class InternationalizationConfig : WebMvcConfigurer {
    var logger = LoggerFactory.getLogger(InternationalizationConfig::class.java)
    @Bean
    open fun localeResolver(): LocaleResolver {
        val cookieLocaleResolver = CookieLocaleResolver()
        // cookieLocaleResolver.cookieMaxAge  = 6 * 30 * 24 * 60 * 60 // 6 mois...
        cookieLocaleResolver.setDefaultLocale(Locale.US)
        // called before launching tomcat :
        // logger.info("resolve locale2 : " + cookieLocaleResolver.toString());
        return cookieLocaleResolver
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        val localeChangeInterceptor = LocaleChangeInterceptor()
        localeChangeInterceptor.paramName = "lang"
        registry.addInterceptor(localeChangeInterceptor).addPathPatterns("/*")
    }

    @Bean(name = ["messageSource"])
    fun messageSource(): MessageSource {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasenames("i18n/messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    private fun messageSourceValidator(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:/i18n/validationMessages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    @Bean
    fun validator(): LocalValidatorFactoryBean {
        val bean = LocalValidatorFactoryBean()
        bean.setValidationMessageSource(messageSourceValidator())
        return bean
    }
    /**
     * This interceptor allows visitors to change the locale
     *
     * @return a LocaleChangeInterceptor object
     */
    //  @Bean
    //  public LocaleChangeInterceptor localeChangeInterceptor() {
    //    LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
    //    //the request param that we'll use to determine the locale
    //    interceptor.setParamName("lang");
    //    return interceptor;
    //  }
}