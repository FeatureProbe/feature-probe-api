package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.dto.DictionaryResponse;
import com.featureprobe.api.service.DictionaryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/dictionaries")
@RestController
@DefaultApiResponses
@AllArgsConstructor
public class DictionaryController {

    private DictionaryService dictionaryService;

    @GetMapping("/{key}")
    public DictionaryResponse query(@PathVariable("key") String key) {
        return dictionaryService.query(key);
    }

    @PostMapping("/{key}")
    public DictionaryResponse save(@PathVariable("key") String key,
                                   @RequestBody String value) {
        return dictionaryService.save(key, value);
    }

}
