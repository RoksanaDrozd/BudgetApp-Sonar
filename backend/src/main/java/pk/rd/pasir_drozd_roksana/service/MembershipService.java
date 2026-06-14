package pk.rd.pasir_drozd_roksana.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pk.rd.pasir_drozd_roksana.dto.MembershipDTO;
import pk.rd.pasir_drozd_roksana.model.Group;
import pk.rd.pasir_drozd_roksana.model.Membership;
import pk.rd.pasir_drozd_roksana.model.User;
import pk.rd.pasir_drozd_roksana.repository.GroupRepository;
import pk.rd.pasir_drozd_roksana.repository.MembershipRepository;
import pk.rd.pasir_drozd_roksana.repository.UserRepository;

import java.util.List;

@Service
public class MembershipService {

    // DODANA STAŁA DLA UCISZENIA SONARA (CRITICAL ISSUE)
    private static final String NOT_FOUND_GROUP_MSG = "Nie znaleziono grupy o ID: ";

    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public MembershipService(MembershipRepository membershipRepository,
                             GroupRepository groupRepository,
                             UserRepository userRepository,
                             CurrentUserService currentUserService) {
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public List<Membership> getGroupMembers(Long groupId) {
        assertCurrentUserIsGroupMember(groupId);
        return membershipRepository.findByGroupId(groupId);
    }

    public Membership addMember(MembershipDTO membershipDTO) {
        assertCurrentUserIsGroupOwner(membershipDTO.getGroupId());

        User user = userRepository.findByEmail(membershipDTO.getUserEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie znaleziono użytkownika o emailu: " + membershipDTO.getUserEmail()));
        Group group = groupRepository.findById(membershipDTO.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException(
                        NOT_FOUND_GROUP_MSG + membershipDTO.getGroupId()));

        boolean alreadyMember = membershipRepository.findByGroupId(group.getId()).stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (alreadyMember) {
            throw new IllegalStateException("Użytkownik jest już członkiem tej grupy.");
        }

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setGroup(group);
        return membershipRepository.save(membership);
    }

    public void removeMember(Long membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Członkostwo nie istnieje."));

        User currentUser = currentUserService.getCurrentUser();
        User groupOwner = membership.getGroup().getOwner();

        if (!currentUser.getId().equals(groupOwner.getId())) {
            throw new AccessDeniedException("Tylko właściciel grupy może usuwać członków.");
        }

        if (membership.getUser().getId().equals(groupOwner.getId())) {
            throw new IllegalStateException("Nie można usunąć właściciela z jego grupy.");
        }

        membershipRepository.delete(membership);
    }

    public void assertCurrentUserIsGroupMember(Long groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(
                        NOT_FOUND_GROUP_MSG + groupId));
        User currentUser = currentUserService.getCurrentUser();
        assertUserIsGroupMember(groupId, currentUser.getId());
    }

    public void assertCurrentUserIsGroupOwner(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(
                        NOT_FOUND_GROUP_MSG + groupId));
        User currentUser = currentUserService.getCurrentUser();
        if (!group.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Tylko właściciel grupy może wykonać tę operację.");
        }
    }

    public void assertUserIsGroupMember(Long groupId, Long userId) {
        if (!membershipRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AccessDeniedException("Użytkownik nie jest członkiem tej grupy.");
        }
    }
}