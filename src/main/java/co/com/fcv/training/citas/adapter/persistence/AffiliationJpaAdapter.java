package co.com.fcv.training.citas.adapter.persistence;

import co.com.fcv.training.citas.application.Ports;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
class AffiliationJpaAdapter implements Ports.Affiliations {
    private final EpsPlansJpa plans;
    private final AffiliationsJpa affiliations;
    private final UsersJpa users;

    AffiliationJpaAdapter(EpsPlansJpa plans, AffiliationsJpa affiliations, UsersJpa users) {
        this.plans = plans;
        this.affiliations = affiliations;
        this.users = users;
    }

    @Override @Transactional(readOnly = true)
    public Optional<Ports.InsurancePlan> activePlan(Long planId) {
        if (planId == null) return Optional.empty();
        return plans.findActiveById(planId).map(plan -> new Ports.InsurancePlan(plan.id));
    }

    @Override @Transactional
    public void createInitial(Long userId, Long planId) {
        if (affiliations.existsByUser_IdAndCurrentTrue(userId)) {
            throw new IllegalArgumentException("El usuario ya tiene una afiliación vigente");
        }
        EpsPlanEntity plan = plans.findActiveById(planId)
                .orElseThrow(() -> new IllegalArgumentException("El plan seleccionado no está disponible"));
        UserInsuranceAffiliationEntity affiliation = new UserInsuranceAffiliationEntity();
        affiliation.user = users.getReferenceById(userId);
        affiliation.plan = plan;
        affiliation.current = true;
        affiliations.saveAndFlush(affiliation);
    }
}
