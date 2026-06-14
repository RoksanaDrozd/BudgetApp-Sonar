package pk.rd.pasir_drozd_roksana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.rd.pasir_drozd_roksana.model.Group;
import pk.rd.pasir_drozd_roksana.model.User;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByMemberships_User(User user);
}