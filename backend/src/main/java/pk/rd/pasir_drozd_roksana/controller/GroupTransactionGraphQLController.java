package pk.rd.pasir_drozd_roksana.controller;

import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import pk.rd.pasir_drozd_roksana.dto.GroupTransactionDTO;
import pk.rd.pasir_drozd_roksana.service.GroupTransactionService;

@Controller
public class GroupTransactionGraphQLController {

    private final GroupTransactionService groupTransactionService;

    public GroupTransactionGraphQLController(GroupTransactionService groupTransactionService) {
        this.groupTransactionService = groupTransactionService;
    }

    @MutationMapping
    public Boolean addGroupTransaction(@Valid @Argument GroupTransactionDTO groupTransactionDTO) {
        return groupTransactionService.addGroupTransaction(groupTransactionDTO);
    }
}