package co.com.fcv.training.citas.application;

import co.com.fcv.training.citas.domain.Account;
import co.com.fcv.training.citas.domain.Identity;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class ProfessionalService {
    public record Create(String firstName, String lastName, String documentType, String documentNumber,
                         String email, String phone, String temporaryPassword,
                         String professionalCode, String licenseNumber) {}

    private final Ports.Accounts accounts;
    private final Ports.Passwords passwords;
    private final Ports.Offer offer;
    private final Ports.Transactions transactions;

    public ProfessionalService(Ports.Accounts accounts, Ports.Passwords passwords, Ports.Offer offer,
                               Ports.Transactions transactions) {
        this.accounts = accounts; this.passwords = passwords; this.offer = offer; this.transactions = transactions;
    }

    public Ports.ProfessionalView create(Create input) {
        return transactions.run(() -> {
            String email = Identity.email(input.email());
            String type = Identity.documentType(input.documentType());
            String number = Identity.required(input.documentNumber());
            if (accounts.existsEmail(email) || accounts.existsDocument(type, number)) {
                throw new IllegalArgumentException("Email o documento ya registrado");
            }
            if (input.temporaryPassword() == null || input.temporaryPassword().isBlank()
                    || input.temporaryPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException("Contraseña temporal inválida");
            }
            Account account = new Account(null, Identity.required(input.firstName()), Identity.required(input.lastName()),
                    type, number, email, Identity.required(input.phone()), passwords.hash(input.temporaryPassword()),
                    Set.of("PROFESSIONAL"));
            Account saved = accounts.save(account);
            return offer.createProfessional(saved.id(), Identity.required(input.professionalCode()),
                    Identity.required(input.licenseNumber()));
        });
    }

    public void assignSpecialties(Long id, List<Short> specialtyIds, Short primaryId) {
        offer.replaceSpecialties(id, specialtyIds, primaryId);
    }
    public void assignLocations(Long id, List<Short> locationIds) { offer.replaceLocations(id, locationIds); }
    public void setActive(Long id, boolean active) { offer.setProfessionalActive(id, active); }
}
