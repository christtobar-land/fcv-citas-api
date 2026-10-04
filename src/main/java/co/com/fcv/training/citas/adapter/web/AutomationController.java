package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.automation.AutomationQueries;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Endpoints de solo lectura para n8n. Protegidos por la clave de servicio (header X-Api-Key). */
@RestController
@RequestMapping("/api/v1/automation")
class AutomationController {
    private final AutomationQueries queries;

    AutomationController(AutomationQueries queries) {
        this.queries = queries;
    }

    @GetMapping("/appointments/upcoming")
    List<AutomationQueries.ReminderDto> upcoming(@RequestParam(defaultValue = "24") int hours) {
        return queries.upcomingApproved(hours);
    }

    @GetMapping("/summary/daily")
    AutomationQueries.DailySummary daily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return queries.dailySummary(date);
    }
}
