package tech.provokedynamic.gymcrmworkload.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.document.MonthSummary;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;
import tech.provokedynamic.gymcrmworkload.document.YearSummary;
import tech.provokedynamic.gymcrmworkload.exception.InvalidWorkloadEventException;
import tech.provokedynamic.gymcrmworkload.repository.TrainerWorkloadRepository;
import tech.provokedynamic.gymcrmworkload.validation.WorkloadEventValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadServiceImpl")
class WorkloadServiceImplTest {

    @Mock
    TrainerWorkloadRepository repository;

    WorkloadEventValidator validator = new WorkloadEventValidator();

    WorkloadServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new WorkloadServiceImpl(repository, validator);
    }

    private WorkloadEvent addEvent(LocalDate date, int duration) {
        return new WorkloadEvent("jane.smith", "Jane", "Smith", true, date, duration,
                WorkloadEvent.ActionType.ADD);
    }

    @Nested
    @DisplayName("processWorkload() - new trainer")
    class NewTrainer {

        @Test
        @DisplayName("creates a document with the year/month/duration derived from the event")
        void createsNewDocument() {
            when(repository.findByTrainerUsername("jane.smith")).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(addEvent(LocalDate.of(2025, 6, 10), 60), "tx-1");

            var captor = ArgumentCaptor.forClass(TrainerWorkloadDocument.class);
            verify(repository).save(captor.capture());

            var saved = captor.getValue();
            assertThat(saved.getTrainerUsername()).isEqualTo("jane.smith");
            assertThat(saved.getTrainerStatus()).isTrue();
            assertThat(saved.getYears()).hasSize(1);
            assertThat(saved.getYears().getFirst().getYear()).isEqualTo(2025);
            assertThat(saved.getYears().getFirst().getMonths()).hasSize(1);
            assertThat(saved.getYears().getFirst().getMonths().getFirst().getMonth()).isEqualTo(6);
            assertThat(saved.getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration())
                    .isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("processWorkload() - existing trainer")
    class ExistingTrainer {

        private TrainerWorkloadDocument existingDocWithMonth(int year, int month, int duration) {
            var doc = new TrainerWorkloadDocument("jane.smith", "Jane", "Smith", true);
            doc.setYears(new java.util.ArrayList<>(List.of(
                    new YearSummary(year, new java.util.ArrayList<>(List.of(
                            new MonthSummary(month, duration)))))));
            return doc;
        }

        @Test
        @DisplayName("adds to the existing month's duration when the month already exists")
        void addsToExistingMonth() {
            var existing = existingDocWithMonth(2025, 6, 100);
            when(repository.findByTrainerUsername("jane.smith")).thenReturn(Optional.of(existing));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(addEvent(LocalDate.of(2025, 6, 15), 30), "tx-2");

            assertThat(existing.getYears()).hasSize(1);
            assertThat(existing.getYears().getFirst().getMonths()).hasSize(1);
            assertThat(existing.getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration())
                    .isEqualTo(130);
        }

        @Test
        @DisplayName("adds a new month entry when the year exists but the month doesn't")
        void addsNewMonthToExistingYear() {
            var existing = existingDocWithMonth(2025, 6, 100);
            when(repository.findByTrainerUsername("jane.smith")).thenReturn(Optional.of(existing));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(addEvent(LocalDate.of(2025, 7, 1), 45), "tx-3");

            assertThat(existing.getYears()).hasSize(1);
            assertThat(existing.getYears().getFirst().getMonths()).hasSize(2);
        }

        @Test
        @DisplayName("subtracts duration on DELETE and floors at zero")
        void deleteFloorsAtZero() {
            var existing = existingDocWithMonth(2025, 6, 20);
            when(repository.findByTrainerUsername("jane.smith")).thenReturn(Optional.of(existing));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var deleteEvent = new WorkloadEvent("jane.smith", "Jane", "Smith", true,
                    LocalDate.of(2025, 6, 15), 50, WorkloadEvent.ActionType.DELETE);

            service.processWorkload(deleteEvent, "tx-4");

            assertThat(existing.getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("throws and never saves when trainerUsername is blank")
        void blankUsername() {
            var event = new WorkloadEvent("", "Jane", "Smith", true,
                    LocalDate.now(), 30, WorkloadEvent.ActionType.ADD);

            assertThatThrownBy(() -> service.processWorkload(event, "tx-5"))
                    .isInstanceOf(InvalidWorkloadEventException.class);

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("throws when trainingDuration is not positive")
        void nonPositiveDuration() {
            var event = new WorkloadEvent("jane.smith", "Jane", "Smith", true,
                    LocalDate.now(), 0, WorkloadEvent.ActionType.ADD);

            assertThatThrownBy(() -> service.processWorkload(event, "tx-6"))
                    .isInstanceOf(InvalidWorkloadEventException.class);

            verify(repository, never()).save(any());
        }
    }
}
