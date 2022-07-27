package com.featureprobe.api.service;

import com.featureprobe.api.auth.UserPasswordAuthenticationToken;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ForbiddenException;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.DictionaryResponse;
import com.featureprobe.api.entity.Dictionary;
import com.featureprobe.api.mapper.DictionaryMapper;
import com.featureprobe.api.repository.DictionaryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class DictionaryService {

    private DictionaryRepository dictionaryRepository;


    public DictionaryResponse save(String key, String value) {
        Optional<Dictionary> dictionaryOptional = dictionaryRepository.findByAccountAndKey(findLoggedInAccount(), key);
        if(dictionaryOptional.isPresent()) {
            Dictionary dictionary = dictionaryOptional.get();
            dictionary.setValue(value);
            dictionary.setAccount(findLoggedInAccount());
            return DictionaryMapper.INSTANCE.entityToResponse(dictionaryRepository.save(dictionary));
        }
        Dictionary dictionary = new Dictionary();
        dictionary.setKey(key);
        dictionary.setValue(value);
        dictionary.setAccount(findLoggedInAccount());
        return DictionaryMapper.INSTANCE.entityToResponse(dictionaryRepository.save(dictionary));
    }
    
    public DictionaryResponse query(String key) {
        Dictionary dictionary = dictionaryRepository.findByAccountAndKey(findLoggedInAccount(), key).orElseThrow(() ->
                new ResourceNotFoundException(ResourceType.DICTIONARY, key));
        return DictionaryMapper.INSTANCE.entityToResponse(dictionary);
    }



    private String findLoggedInAccount() {
        UserPasswordAuthenticationToken authentication =
                (UserPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            throw new ForbiddenException();
        }
        return authentication.getAccount();
    }
}
