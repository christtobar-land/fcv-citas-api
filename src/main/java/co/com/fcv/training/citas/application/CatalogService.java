package co.com.fcv.training.citas.application;

import java.util.List;

public class CatalogService {
    private final Ports.Catalogs catalogs;

    public CatalogService(Ports.Catalogs catalogs) { this.catalogs = catalogs; }
    public List<Ports.RoleCatalog> roles() { return catalogs.roles(); }
    public List<Ports.StatusCatalog> appointmentStatuses() { return catalogs.appointmentStatuses(); }
    public List<Ports.StatusCatalog> rescheduleStatuses() { return catalogs.rescheduleStatuses(); }
    public List<Ports.RegimeCatalog> insuranceRegimes() { return catalogs.insuranceRegimes(); }
    public List<Ports.LocationCatalog> locations() { return catalogs.locations(); }
    public List<Ports.InsurancePlanCatalog> insurancePlans() { return catalogs.insurancePlans(); }
}
