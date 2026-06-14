package pk.rd.pasir_drozd_roksana.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pk.rd.pasir_drozd_roksana.dto.DebtDTO;
import pk.rd.pasir_drozd_roksana.model.Debt;
import pk.rd.pasir_drozd_roksana.model.Group;
import pk.rd.pasir_drozd_roksana.model.User;
import pk.rd.pasir_drozd_roksana.repository.DebtRepository;
import pk.rd.pasir_drozd_roksana.repository.GroupRepository;
import pk.rd.pasir_drozd_roksana.repository.UserRepository;

import java.util.List;

@Service
public class DebtService {

    // STAŁA DODANA DLA SONARA:
    private static final String NOT_FOUND_MSG = " nie istnieje.";

    private final DebtRepository debtRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MembershipService membershipService;
    private final CurrentUserService currentUserService;

    public DebtService(DebtRepository debtRepository,
                       GroupRepository groupRepository,
                       UserRepository userRepository,
                       MembershipService membershipService,
                       CurrentUserService currentUserService) {
        this.debtRepository = debtRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.membershipService = membershipService;
        this.currentUserService = currentUserService;
    }

    public List<Debt> getGroupDebts(Long groupId) {
        membershipService.assertCurrentUserIsGroupMember(groupId);
        return debtRepository.findByGroupId(groupId);
    }

    public Debt createDebt(DebtDTO debtDTO) {
        // UŻYCIE STAŁEJ:
        Group group = groupRepository.findById(debtDTO.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie można utworzyć długu. Grupa o ID " + debtDTO.getGroupId() + NOT_FOUND_MSG));
        
        // UŻYCIE STAŁEJ:
        User debtor = userRepository.findById(debtDTO.getDebtorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie można utworzyć długu. Dłużnik o ID " + debtDTO.getDebtorId() + NOT_FOUND_MSG));
        
        // UŻYCIE STAŁEJ:
        User creditor = userRepository.findById(debtDTO.getCreditorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie można utworzyć długu. Wierzyciel o ID " + debtDTO.getCreditorId() + NOT_FOUND_MSG));

        membershipService.assertCurrentUserIsGroupMember(group.getId());
        membershipService.assertUserIsGroupMember(group.getId(), debtor.getId());
        membershipService.assertUserIsGroupMember(group.getId(), creditor.getId());

        if (debtor.getId().equals(creditor.getId())) {
            throw new IllegalStateException("Dłużnik i wierzyciel muszą być różnymi użytkownikami.");
        }

        User currentUser = currentUserService.getCurrentUser();
        assertCurrentUserCanManageDebt(group, debtor, creditor, currentUser);

        Debt debt = new Debt();
        debt.setGroup(group);
        debt.setDebtor(debtor);
        debt.setCreditor(creditor);
        debt.setAmount(debtDTO.getAmount());
        debt.setTitle(debtDTO.getTitle());
        return debtRepository.save(debt);
    }

    public void deleteDebt(Long debtId) {
        // UŻYCIE STAŁEJ:
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie można usunąć długu. Dług o ID " + debtId + NOT_FOUND_MSG));
        membershipService.assertCurrentUserIsGroupMember(debt.getGroup().getId());
        User currentUser = currentUserService.getCurrentUser();
        assertCurrentUserCanManageDebt(debt.getGroup(), debt.getDebtor(), debt.getCreditor(), currentUser);
        debtRepository.delete(debt);
    }

    private void assertCurrentUserCanManageDebt(Group group, User debtor, User creditor, User currentUser) {
        boolean isGroupOwner = group.getOwner().getId().equals(currentUser.getId());
        boolean isDebtParticipant = debtor.getId().equals(currentUser.getId())
                || creditor.getId().equals(currentUser.getId());
        if (!isGroupOwner && !isDebtParticipant) {
            throw new AccessDeniedException("Tylko właściciel grupy albo uczestnik długu może wykonać te operacje.");
        }
    }
}