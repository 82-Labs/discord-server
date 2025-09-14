package com.jydev.discord.config

import com.jydev.discord.domain.user.relation.UserRelationRequestAction
import com.jydev.discord.domain.user.relation.UserRelationType
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class EnumConverterConfig : WebFluxConfigurer {
    
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToUserRelationTypeConverter())
        registry.addConverter(StringToUserRelationRequestActionConverter())
    }
    
    class StringToUserRelationTypeConverter : Converter<String, UserRelationType> {
        override fun convert(source: String): UserRelationType {
            return UserRelationType.valueOf(source.uppercase())
        }
    }
    
    class StringToUserRelationRequestActionConverter : Converter<String, UserRelationRequestAction> {
        override fun convert(source: String): UserRelationRequestAction {
            return UserRelationRequestAction.valueOf(source.uppercase())
        }
    }
}