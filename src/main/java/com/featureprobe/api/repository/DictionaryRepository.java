package com.featureprobe.api.repository;


import com.featureprobe.api.entity.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long>, JpaSpecificationExecutor<Dictionary> {

    Optional<Dictionary> findByAccountAndKey(String account, String key);

}
