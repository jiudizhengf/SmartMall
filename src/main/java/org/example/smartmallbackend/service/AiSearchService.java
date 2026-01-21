package org.example.smartmallbackend.service;

import org.example.smartmallbackend.entity.PmsSpu;

import java.util.List;

public interface AiSearchService {
    List<PmsSpu> search(String keyword);
}
