package com.fighthub.service;

import com.fighthub.dto.dashboard.AlunosFaltasResponse;
import com.fighthub.dto.dashboard.DashboardResponse;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.AulaRepository;
import com.fighthub.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private AulaRepository aulaRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private YearMonth sampleMonth;
    private LocalDate start;
    private LocalDate end;

    @BeforeEach
    void setup() {
        sampleMonth = YearMonth.of(2025, 12);
        start = sampleMonth.atDay(1);
        end = sampleMonth.atEndOfMonth();
    }

    @Test
    void deveRetornarContagensDeAulas_QuandoRepositorioRetornaValoresStubados() {
        when(aulaRepository.countScheduledBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(7L);
        when(aulaRepository.countConductedBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(5L);
        when(aulaRepository.countCanceledBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(2L);

        long scheduled = dashboardService.countAulasPrevistas(sampleMonth);
        long conducted = dashboardService.countAulasRealizadas(sampleMonth);
        long canceled = dashboardService.countAulasCanceladas(sampleMonth);

        assertEquals(7L, scheduled);
        assertEquals(5L, conducted);
        assertEquals(2L, canceled);

        verify(aulaRepository).countScheduledBetween(any(LocalDate.class), any(LocalDate.class));
        verify(aulaRepository).countConductedBetween(any(LocalDate.class), any(LocalDate.class));
        verify(aulaRepository).countCanceledBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void deveRetornarZeroParaMediasDePresenca_QuandoRepositorioRetornaNull() {
        when(aulaRepository.overallAverageAttendancePercentBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(null);
        when(aulaRepository.averageAttendancePercentPerClassBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(null);

        double overall = dashboardService.presenceAverageOverall(sampleMonth);
        double perClass = dashboardService.presenceAveragePerClass(sampleMonth);

        assertEquals(0.0, overall);
        assertEquals(0.0, perClass);

        verify(aulaRepository).overallAverageAttendancePercentBetween(any(LocalDate.class), any(LocalDate.class));
        verify(aulaRepository).averageAttendancePercentPerClassBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void deveMapearTop5AlunosComMaisFaltas_QuandoLinhasRetornadas() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Object[] row1 = new Object[]{id1.toString(), "Alice", 4L};
        Object[] row2 = new Object[]{id2.toString(), "Bob", 2L};

        when(aulaRepository.findTop5AlunosWithMostAbsencesBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(row1, row2));

        List<AlunosFaltasResponse> result = dashboardService.top5AlunosComMaisFaltas(sampleMonth);

        assertNotNull(result);
        assertEquals(2, result.size());

        AlunosFaltasResponse r1 = result.get(0);
        assertEquals(id1, r1.alunoId());
        assertEquals("Alice", r1.nome());
        assertEquals(4L, r1.faltas());

        AlunosFaltasResponse r2 = result.get(1);
        assertEquals(id2, r2.alunoId());
        assertEquals("Bob", r2.nome());
        assertEquals(2L, r2.faltas());

        verify(aulaRepository).findTop5AlunosWithMostAbsencesBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void deveRetornarDashboardComDadosAgregados_QuandoRepositoriosRetornamValores() {
        when(alunoRepository.countByMatriculaAtiva(true)).thenReturn(2L);
        when(alunoRepository.countByMatriculaAtiva(false)).thenReturn(3L);
        when(alunoRepository.countByMatriculaAtivaAndDataMatriculaAfter(eq(true), any(LocalDate.class))).thenReturn(1L);
        when(alunoRepository.sumAgesByMatriculaAtiva(true)).thenReturn(50L);

        when(turmaRepository.countByAtivo(true)).thenReturn(4L);
        when(turmaRepository.countByAtivo(false)).thenReturn(1L);

        when(aulaRepository.calcularOcupacaoMediaAulas()).thenReturn(0.6);
        when(aulaRepository.calcularPercentualAulasLotadas()).thenReturn(12.5);
        when(aulaRepository.calcularMediaAlunosPorAula()).thenReturn(8.0);

        when(aulaRepository.countScheduledBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(10L);
        when(aulaRepository.countConductedBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(8L);
        when(aulaRepository.countCanceledBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(2L);

        when(aulaRepository.overallAverageAttendancePercentBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(75.0);
        when(aulaRepository.averageAttendancePercentPerClassBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(70.0);

        Object[] row = new Object[]{UUID.randomUUID().toString(), "Charlie", 3L};
        when(aulaRepository.findTop5AlunosWithMostAbsencesBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(row));

        DashboardResponse response = dashboardService.getDashboardData();

        assertNotNull(response);

        verify(alunoRepository, times(2)).countByMatriculaAtiva(true);
        verify(turmaRepository).countByAtivo(true);
        verify(aulaRepository).calcularOcupacaoMediaAulas();
        verify(aulaRepository).overallAverageAttendancePercentBetween(any(LocalDate.class), any(LocalDate.class));
        verify(aulaRepository).findTop5AlunosWithMostAbsencesBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void deveRetornarListaVaziaTop5_QuandoRepositorioRetornaVazio() {
        when(aulaRepository.findTop5AlunosWithMostAbsencesBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        List<AlunosFaltasResponse> result = dashboardService.top5AlunosComMaisFaltas(sampleMonth);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(aulaRepository).findTop5AlunosWithMostAbsencesBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void deveTratarOcupacaoMediaNula_RetornandoZero() {
        when(alunoRepository.countByMatriculaAtiva(true)).thenReturn(0L);
        when(alunoRepository.countByMatriculaAtiva(false)).thenReturn(0L);
        when(alunoRepository.countByMatriculaAtivaAndDataMatriculaAfter(eq(true), any(LocalDate.class))).thenReturn(0L);
        when(alunoRepository.sumAgesByMatriculaAtiva(true)).thenReturn(0L);
        when(turmaRepository.countByAtivo(true)).thenReturn(0L);
        when(turmaRepository.countByAtivo(false)).thenReturn(0L);

        when(aulaRepository.calcularOcupacaoMediaAulas()).thenReturn(null);

        DashboardResponse response = dashboardService.getDashboardData();

        assertNotNull(response);
        assertEquals(0.0, response.dadosTurmas().ocupacaoMediaTurmas());
    }

    @Test
    void deveRetornarIdadeMediaZero_QuandoNenhumAlunoAtivoOuSomaIdadesZero() {
        when(alunoRepository.countByMatriculaAtiva(true)).thenReturn(0L);
        when(alunoRepository.sumAgesByMatriculaAtiva(true)).thenReturn(0L);

        when(turmaRepository.countByAtivo(true)).thenReturn(0L);
        when(turmaRepository.countByAtivo(false)).thenReturn(0L);
        when(aulaRepository.calcularOcupacaoMediaAulas()).thenReturn(0.1);
        when(aulaRepository.calcularPercentualAulasLotadas()).thenReturn(0.0);
        when(aulaRepository.calcularMediaAlunosPorAula()).thenReturn(0.0);
        when(aulaRepository.findTop5AlunosWithMostAbsencesBetween(any(), any())).thenReturn(Collections.emptyList());
        when(aulaRepository.overallAverageAttendancePercentBetween(any(), any())).thenReturn(0.0);
        when(aulaRepository.averageAttendancePercentPerClassBetween(any(), any())).thenReturn(0.0);

        DashboardResponse response = dashboardService.getDashboardData();

        assertEquals(0, response.dadosAlunos().idadeMediaAlunos());
    }

    @Test
    void deveRetornarIdadeMediaMaxInt_QuandoSomaIdadesExtrapolaInt() {
        when(alunoRepository.countByMatriculaAtiva(true)).thenReturn(1L);
        when(alunoRepository.sumAgesByMatriculaAtiva(true)).thenReturn((long) Integer.MAX_VALUE + 100L);

        when(turmaRepository.countByAtivo(true)).thenReturn(0L);
        when(turmaRepository.countByAtivo(false)).thenReturn(0L);
        when(aulaRepository.calcularOcupacaoMediaAulas()).thenReturn(0.1);
        when(aulaRepository.calcularPercentualAulasLotadas()).thenReturn(0.0);
        when(aulaRepository.calcularMediaAlunosPorAula()).thenReturn(0.0);
        when(aulaRepository.findTop5AlunosWithMostAbsencesBetween(any(), any())).thenReturn(Collections.emptyList());
        when(aulaRepository.overallAverageAttendancePercentBetween(any(), any())).thenReturn(0.0);
        when(aulaRepository.averageAttendancePercentPerClassBetween(any(), any())).thenReturn(0.0);

        DashboardResponse response = dashboardService.getDashboardData();

        assertEquals(Integer.MAX_VALUE, response.dadosAlunos().idadeMediaAlunos());
    }

    @Test
    void deveMapearTop5ComValoresNulos_QuandoLinhasContemNulos() {
        Object[] row = new Object[]{null, null, null};
        when(aulaRepository.findTop5AlunosWithMostAbsencesBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(row));

        List<AlunosFaltasResponse> result = dashboardService.top5AlunosComMaisFaltas(sampleMonth);

        assertNotNull(result);
        assertEquals(1, result.size());
        AlunosFaltasResponse r = result.get(0);
        assertNull(r.alunoId());
        assertNull(r.nome());
        assertEquals(0L, r.faltas());
    }

    @Test
    void deveRetornarListaVaziaTop5_QuandoRepositorioRetornaNull() {
        when(aulaRepository.findTop5AlunosWithMostAbsencesBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(null);

        List<AlunosFaltasResponse> result = dashboardService.top5AlunosComMaisFaltas(sampleMonth);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void deveRetornarValoresForaIntervalo_ParaMediasDePresenca() {
        when(aulaRepository.overallAverageAttendancePercentBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(150.0);
        when(aulaRepository.averageAttendancePercentPerClassBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(-5.0);

        assertEquals(150.0, dashboardService.presenceAverageOverall(sampleMonth));
        assertEquals(-5.0, dashboardService.presenceAveragePerClass(sampleMonth));
    }

    @Test
    void deveTratarPercentualEMediaNulos_QuandoGetDashboardData() {
        when(alunoRepository.countByMatriculaAtiva(true)).thenReturn(0L);
        when(alunoRepository.countByMatriculaAtiva(false)).thenReturn(0L);
        when(alunoRepository.countByMatriculaAtivaAndDataMatriculaAfter(eq(true), any(LocalDate.class))).thenReturn(0L);
        when(alunoRepository.sumAgesByMatriculaAtiva(true)).thenReturn(0L);

        when(turmaRepository.countByAtivo(true)).thenReturn(1L);
        when(turmaRepository.countByAtivo(false)).thenReturn(0L);

        when(aulaRepository.calcularOcupacaoMediaAulas()).thenReturn(0.5);
        when(aulaRepository.calcularPercentualAulasLotadas()).thenReturn(null);
        when(aulaRepository.calcularMediaAlunosPorAula()).thenReturn(null);

        when(aulaRepository.findTop5AlunosWithMostAbsencesBetween(any(), any())).thenReturn(Collections.emptyList());
        when(aulaRepository.overallAverageAttendancePercentBetween(any(), any())).thenReturn(0.0);
        when(aulaRepository.averageAttendancePercentPerClassBetween(any(), any())).thenReturn(0.0);
        when(aulaRepository.countScheduledBetween(any(), any())).thenReturn(0L);
        when(aulaRepository.countConductedBetween(any(), any())).thenReturn(0L);
        when(aulaRepository.countCanceledBetween(any(), any())).thenReturn(0L);

        DashboardResponse response = dashboardService.getDashboardData();

        assertEquals(0.0, response.dadosTurmas().percentualAulasLotadas());
        assertEquals(0.0, response.dadosTurmas().mediaAlunosPorAula());
    }

}