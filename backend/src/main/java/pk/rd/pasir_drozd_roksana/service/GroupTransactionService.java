package pk.rd.pasir_drozd_roksana.service;

import jakarta.persistence.EntityNotFoundException;
import pk.rd.pasir_drozd_roksana.dto.GroupTransactionDTO;
import pk.rd.pasir_drozd_roksana.model.*;
import pk.rd.pasir_drozd_roksana.repository.DebtRepository;
import pk.rd.pasir_drozd_roksana.repository.GroupRepository;
import pk.rd.pasir_drozd_roksana.repository.MembershipRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupTransactionService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final CurrentUserService currentUserService;
    private final GroupNotificationService groupNotificationService;

    public GroupTransactionService(GroupRepository groupRepository,
                                   MembershipRepository membershipRepository,
                                   DebtRepository debtRepository,
                                   CurrentUserService currentUserService,
                                   GroupNotificationService groupNotificationService) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.debtRepository = debtRepository;
        this.currentUserService = currentUserService;
        this.groupNotificationService = groupNotificationService;
    }

    public boolean addGroupTransaction(GroupTransactionDTO dto) {
        User currentUser = currentUserService.getCurrentUser();

        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono grupy"));

        List<Membership> members = membershipRepository.findByGroupId(group.getId());

        if (members.isEmpty()) {
            throw new IllegalStateException("Grupa nie ma członków, nie można dodać transakcji.");
        }

        double amountPerUser = dto.getAmount() / members.size();
        boolean expense = "EXPENSE".equals(dto.getType());

        for (Membership member : members) {
            User otherUser = member.getUser();
            if (!otherUser.getId().equals(currentUser.getId())) {
                Debt debt = new Debt();
                debt.setGroup(group);
                debt.setAmount(amountPerUser);
                debt.setTitle(dto.getTitle());
                if (expense) {
                    debt.setDebtor(otherUser);
                    debt.setCreditor(currentUser);
                } else {
                    debt.setDebtor(currentUser);
                    debt.setCreditor(otherUser);
                }
                debtRepository.save(debt);
            }
        }

        groupNotificationService.notifyGroupExpenseAdded(
                group,
                dto.getTitle(),
                dto.getAmount(),
                amountPerUser,
                currentUser,
                members
        );

        return true;
    }
}