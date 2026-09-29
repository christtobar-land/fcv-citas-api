package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SpecialtyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogs")
class SchedulingCatalogController {
    private final SpecialtyService specialties;
    SchedulingCatalogController(SpecialtyService specialties) { this.specialties = specialties; }
    @GetMapping("/specialties")
    List<Ports.SpecialtyView> specialties() { return specialties.active(); }
}
