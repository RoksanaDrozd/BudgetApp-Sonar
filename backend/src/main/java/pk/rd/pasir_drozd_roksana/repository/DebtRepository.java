package pk.rd.pasir_drozd_roksana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.rd.pasir_drozd_roksana.model.Debt;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    List<Debt> findByGroupId(Long groupId);

    void deleteByGroupId(Long groupId);
}