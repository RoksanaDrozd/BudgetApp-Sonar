package pk.rd.pasir_drozd_roksana.service;

import pk.rd.pasir_drozd_roksana.dto.GroupNotificationDTO;
import pk.rd.pasir_drozd_roksana.model.Group;
import pk.rd.pasir_drozd_roksana.model.Membership;
import pk.rd.pasir_drozd_roksana.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public GroupNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyGroupExpenseAdded(Group group,
                                        String title,
                                        Double amount,
                                        Double userShare,
                                        User createdBy,
                                        List<Membership> selectedMembers) {

        for (Membership member : selectedMembers) {
            User user = member.getUser();

            if (!user.getId().equals(createdBy.getId())) {
                GroupNotificationDTO notification = new GroupNotificationDTO(
                        "GROUP_EXPENSE_ADDED",
                        group.getId(),
                        group.getName(),
                        title,
                        amount,
                        userShare,
                        createdBy.getEmail(),
                        createdBy.getEmail()
                                + " dodal wydatek \""
                                + title
                                + "\" w grupie "
                                + group.getName()
                                + ". Twoja czesc: "
                                + String.format("%.2f", userShare)
                                + " zl."
                );

                messagingTemplate.convertAndSend(
                        "/topic/group-notifications/" + user.getEmail(),
                        notification
                );
            }
        }
    }
}