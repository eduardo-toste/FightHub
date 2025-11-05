package com.fighthub.service;

import com.fighthub.repository.AulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AulaService {

    private final AulaRepository aulaRepository;

}
