package co.com.fcv.training.citas.adapter.persistence;

import co.com.fcv.training.citas.application.Ports;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
class CatalogJpaAdapter implements Ports.Catalogs {
    private final RolesJpa roles;
    private final AppointmentStatusesJpa appointmentStatuses;
    private final RescheduleStatusesJpa rescheduleStatuses;
    private final InsuranceRegimesJpa insuranceRegimes;
    private final LocationsJpa locations;

    CatalogJpaAdapter(RolesJpa roles, AppointmentStatusesJpa appointmentStatuses,
                      RescheduleStatusesJpa rescheduleStatuses, InsuranceRegimesJpa insuranceRegimes,
                      LocationsJpa locations) {
        this.roles = roles;
        this.appointmentStatuses = appointmentStatuses;
        this.rescheduleStatuses = rescheduleStatuses;
        this.insuranceRegimes = insuranceRegimes;
        this.locations = locations;
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.RoleCatalog> roles() {
        return roles.findAll().stream().map(e -> new Ports.RoleCatalog(e.id, e.code, e.name, e.description)).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.StatusCatalog> appointmentStatuses() {
        return appointmentStatuses.findAll().stream().map(e -> new Ports.StatusCatalog(e.id, e.code, e.name, e.terminal)).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.StatusCatalog> rescheduleStatuses() {
        return rescheduleStatuses.findAll().stream().map(e -> new Ports.StatusCatalog(e.id, e.code, e.name, e.terminal)).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.RegimeCatalog> insuranceRegimes() {
        return insuranceRegimes.findAll().stream().map(e -> new Ports.RegimeCatalog(e.id, e.code, e.name)).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.LocationCatalog> locations() {
        return locations.findAll().stream().map(e -> new Ports.LocationCatalog(e.id, e.code, e.name, e.address, e.city, e.department, e.active)).toList();
    }
}
