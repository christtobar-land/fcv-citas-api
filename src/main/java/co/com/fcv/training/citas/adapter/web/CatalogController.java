package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.CatalogService;
import co.com.fcv.training.citas.application.Ports;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogs")
class CatalogController {
    private final CatalogService catalogs;
    CatalogController(CatalogService catalogs) { this.catalogs = catalogs; }

    @GetMapping("/roles") List<Ports.RoleCatalog> roles() { return catalogs.roles(); }
    @GetMapping("/appointment-statuses") List<Ports.StatusCatalog> appointmentStatuses() { return catalogs.appointmentStatuses(); }
    @GetMapping("/reschedule-statuses") List<Ports.StatusCatalog> rescheduleStatuses() { return catalogs.rescheduleStatuses(); }
    @GetMapping("/insurance-regimes") List<Ports.RegimeCatalog> insuranceRegimes() { return catalogs.insuranceRegimes(); }
    @GetMapping("/locations") List<Ports.LocationCatalog> locations() { return catalogs.locations(); }
}
