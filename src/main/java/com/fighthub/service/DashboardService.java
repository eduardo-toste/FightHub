package com.fighthub.service;

import com.fighthub.dto.dashboard.DashboardResponse;
import com.fighthub.dto.dashboard.GeralDashboardResponse;
import com.fighthub.dto.dashboard.TurmasDashboardResponse;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;

    public DashboardResponse getDashboardData() {
        long alunosAtivos = calcularTotalAlunosAtivos();
        long alunosInativos = calcularTotalAlunosInativos();
        long alunosNovos30Dias = calcularNovosAlunosUltimos30Dias();
        int idadeMediaAlunos = calcularIdadeMediaAlunos();

        long turmasAtivas = calcularTotalTurmasAtivas();
        long turmasInativas = calcularTotalTurmasInativas();

        return new DashboardResponse(
                new GeralDashboardResponse(alunosAtivos, alunosInativos, alunosNovos30Dias, idadeMediaAlunos),
                new TurmasDashboardResponse(turmasAtivas, turmasInativas, 00.0),
                null);
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
}
