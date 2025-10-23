package com.fighthub.controller;

import com.fighthub.repository.ModalidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ModalidadeController {

    private final ModalidadeRepository modalidadeRepository;

}
