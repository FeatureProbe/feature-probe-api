package com.featureprobe.api.service

import com.featureprobe.api.auth.UserPasswordAuthenticationToken
import com.featureprobe.api.entity.Dictionary
import com.featureprobe.api.repository.DictionaryRepository
import org.mockito.Mockito
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import spock.lang.Specification

class DictionaryServiceSpec extends Specification{

    DictionaryRepository dictionaryRepository

    DictionaryService dictionaryService


    def setup () {
        dictionaryRepository = Mock(DictionaryRepository)
        dictionaryService = new DictionaryService(dictionaryRepository)
        Mockito.mockStatic(SecurityContextHolder.class)
    }

    def "save a not exist Dictionary"() {
        given:
        Mockito.when(SecurityContextHolder.getContext()).thenReturn(new SecurityContextImpl(new UserPasswordAuthenticationToken("account", "password")))
        when:
        def dict = dictionaryService.save("key", "value")
        then:
        1 * dictionaryRepository.findByAccountAndKey(_ , "key") >> Optional.of(new Dictionary(key: "key", value: "value"))
        1 * dictionaryRepository.save(_) >> new Dictionary(key: "key", value: "value")
        "value" == dict.value
        "key" == dict.key
    }
}

