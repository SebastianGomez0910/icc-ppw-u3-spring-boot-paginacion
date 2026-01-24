package ec.edu.ups.icc.fundamentos01.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.security.models.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.models.RoleName;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long>{
    
    Optional<RoleEntity> findByName(RoleName name);

    boolean existsByName(RoleName name);
}
