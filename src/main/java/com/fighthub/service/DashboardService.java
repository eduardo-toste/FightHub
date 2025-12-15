package com.fighthub.service;

import com.fighthub.dto.dashboard.*;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.AulaRepository;
import com.fighthub.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final AulaRepository aulaRepository;

    public DashboardResponse getDashboardData() {
        long alunosAtivos = calcularTotalAlunosAtivos();
        long alunosInativos = calcularTotalAlunosInativos();
        long alunosNovos30Dias = calcularNovosAlunosUltimos30Dias();
        int idadeMediaAlunos = calcularIdadeMediaAlunos();

        long turmasAtivas = calcularTotalTurmasAtivas();
        long turmasInativas = calcularTotalTurmasInativas();
        double ocupacaoMediaAulas = calcularOcupacaoMediaAulas();

        double percentualAulasLotadas = calcularPercentualAulasLotadas();
        double mediaAlunosPorAula = calcularMediaAlunosPorAula();

        YearMonth month = YearMonth.now();
        long aulasPrevistas = countAulasPrevistas(month);
        long aulasRealizadas = countAulasRealizadas(month);
        long aulasCanceladas = countAulasCanceladas(month);
        double presenceAvgOverall = presenceAverageOverall(month);
        double presenceAvgPerClass = presenceAveragePerClass(month);
        List<AlunosFaltasResponse> top5Faltas = top5AlunosComMaisFaltas(month);

        return new DashboardResponse(
                new AlunosDashboardResponse(alunosAtivos, alunosInativos, alunosNovos30Dias, idadeMediaAlunos),
                new TurmasDashboardResponse(turmasAtivas, turmasInativas, ocupacaoMediaAulas, percentualAulasLotadas, mediaAlunosPorAula),
                new EngajamentoDashboardResponse(aulasPrevistas, aulasRealizadas, aulasCanceladas, presenceAvgOverall, presenceAvgPerClass, top5Faltas));
    }

    private long calcularTotalAlunosAtivos() {
        return alunoRepository.countByMatriculaAtiva(true);
    }

    private long calcularTotalAlunosInativos() {
        return alunoRepository.countByMatriculaAtiva(false);
    }

    private long calcularNovosAlunosUltimos30Dias() {
        return alunoRepository.countByMatriculaAtivaAndDataMatriculaAfter(true, LocalDate.now().minusDays(30));
    }

    private int calcularIdadeMediaAlunos() {
        long alunosAtivos = calcularTotalAlunosAtivos();
        Long somaIdades = alunoRepository.sumAgesByMatriculaAtiva(true);

        if (alunosAtivos == 0L || somaIdades == null || somaIdades == 0L) {
            return 0;
        }

        long media = somaIdades / alunosAtivos;
        if (media > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) media;
    }

    private long calcularTotalTurmasAtivas() {
        return turmaRepository.countByAtivo(true);
    }

    private long calcularTotalTurmasInativas() {
        return turmaRepository.countByAtivo(false);
    }

    private double calcularOcupacaoMediaAulas() {
        Double val = aulaRepository.calcularOcupacaoMediaAulas();
        return val == null ? 0.0 : val;
    }

    private double calcularPercentualAulasLotadas() {
        Double val = aulaRepository.calcularPercentualAulasLotadas();
        return val == null ? 0.0 : val;
    }

    private double calcularMediaAlunosPorAula() {
        Double val = aulaRepository.calcularMediaAlunosPorAula();
        return val == null ? 0.0 : val;
    }

    public long countAulasPrevistas(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return aulaRepository.countScheduledBetween(start, end);
    }

    public long countAulasRealizadas(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return aulaRepository.countConductedBetween(start, end);
    }

    public long countAulasCanceladas(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return aulaRepository.countCanceledBetween(start, end);
    }

    public double presenceAverageOverall(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        Double val = aulaRepository.overallAverageAttendancePercentBetween(start, end);
        return val == null ? 0.0 : val;
    }

    public double presenceAveragePerClass(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        Double val = aulaRepository.averageAttendancePercentPerClassBetween(start, end);
        return val == null ? 0.0 : val;
    }

    public List<AlunosFaltasResponse> top5AlunosComMaisFaltas(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<Object[]> rows = aulaRepository.findTop5AlunosWithMostAbsencesBetween(start, end);

        if (rows == null) {
            return Collections.emptyList();
        }

        return rows.stream()
                .map(r -> new AlunosFaltasResponse(
                        r[0] == null ? null : UUID.fromString(r[0].toString()),
                        r[1] == null ? null : r[1].toString(),
                        r[2] == null ? 0L : ((Number) r[2]).longValue()
                ))
                .collect(Collectors.toList());
    }
}