package paymentsService.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paymentsService.domain.entity.user.User;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {

}
