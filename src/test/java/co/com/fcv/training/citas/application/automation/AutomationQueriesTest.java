package co.com.fcv.training.citas.application.automation;

import co.com.fcv.training.citas.adapter.persistence.RoleEntity;
import co.com.fcv.training.citas.adapter.persistence.UserEntity;
import co.com.fcv.training.citas.adapter.persistence.medical.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AutomationQueriesTest {

    private final AppointmentRepository repo = Mockito.mock(AppointmentRepository.class);
    private final AutomationQueries queries = new AutomationQueries(repo);

    @Test
    void upcomingApprovedMapsAndFiltersCorrectly() {
        UserEntity patient = Mockito.mock(UserEntity.class);
        when(patient.getId()).thenReturn(1L);
        when(patient.getFirstName()).thenReturn("Carlos");
        when(patient.getEmail()).thenReturn("carlos@example.com");

        UserEntity profUser = Mockito.mock(UserEntity.class);
        when(profUser.getId()).thenReturn(2L);
        when(profUser.getFirstName()).thenReturn("Ana");
        when(profUser.getLastName()).thenReturn("Ruiz");

        ProfessionalEntity prof = new ProfessionalEntity();
        prof.setId(10L);
        prof.setUser(profUser);

        LocationEntity loc = new LocationEntity();
        loc.setId((short) 1);
        loc.setName("Sede Bucaramanga");

        SpecialtyEntity spec = new SpecialtyEntity();
        spec.setId((short) 2);
        spec.setName("Cardiología");

        AppointmentStatusEntity status = new AppointmentStatusEntity();
        status.setCode("APPROVED");
        status.setName("Aprobada");

        AppointmentEntity appt = new AppointmentEntity();
        appt.setId(99L);
        appt.setPatient(patient);
        appt.setProfessional(prof);
        appt.setLocation(loc);
        appt.setSpecialty(spec);
        appt.setStatus(status);
        appt.setScheduledStartAt(LocalDateTime.now().plusHours(2));
        appt.setScheduledEndAt(LocalDateTime.now().plusHours(2).plusMinutes(30));

        when(repo.findApprovedStartingBetween(any(), any())).thenReturn(List.of(appt));

        var results = queries.upcomingApproved(24);
        assertThat(results).hasSize(1);
        var first = results.get(0);
        assertThat(first.appointmentId()).isEqualTo(99L);
        assertThat(first.patientFirstName()).isEqualTo("Carlos");
        assertThat(first.patientEmail()).isEqualTo("carlos@example.com");
        assertThat(first.doctorName()).isEqualTo("Dr(a). Ana Ruiz");
        assertThat(first.specialty()).isEqualTo("Cardiología");
        assertThat(first.location()).isEqualTo("Sede Bucaramanga");
    }

    @Test
    void dailySummaryGroupsProperly() {
        LocationEntity loc = new LocationEntity();
        loc.setName("Sede Principal");

        SpecialtyEntity spec = new SpecialtyEntity();
        spec.setName("Medicina General");

        AppointmentStatusEntity status = new AppointmentStatusEntity();
        status.setCode("APPROVED");

        AppointmentEntity appt1 = new AppointmentEntity();
        appt1.setLocation(loc);
        appt1.setSpecialty(spec);
        appt1.setStatus(status);

        AppointmentEntity appt2 = new AppointmentEntity();
        appt2.setLocation(loc);
        appt2.setSpecialty(spec);
        appt2.setStatus(status);

        when(repo.findStartingBetween(any(), any())).thenReturn(List.of(appt1, appt2));

        var summary = queries.dailySummary(LocalDate.of(2026, 10, 10));
        assertThat(summary.date()).isEqualTo("2026-10-10");
        assertThat(summary.total()).isEqualTo(2);
        assertThat(summary.rows()).hasSize(1);
        assertThat(summary.rows().get(0).total()).isEqualTo(2);
        assertThat(summary.rows().get(0).location()).isEqualTo("Sede Principal");
        assertThat(summary.rows().get(0).specialty()).isEqualTo("Medicina General");
        assertThat(summary.rows().get(0).status()).isEqualTo("APPROVED");
    }
}
