package com.fighthub.service;

import com.fighthub.repository.ProfessorRepository;
import com.fighthub.repository.TurmaRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

    @Mock private TurmaRepository turmaRepository;
    @Mock private ProfessorRepository professorRepository;

    @InjectMocks private TurmaService turmaService;


}