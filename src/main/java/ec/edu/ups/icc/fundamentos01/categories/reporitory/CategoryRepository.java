package ec.edu.ups.icc.fundamentos01.categories.reporitory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

        /**
         * Verifica si ya existe una categoría con ese nombre
         * Útil para validaciones de unicidad
         */
        boolean existsByName(String name);

        /**
         * Busca categoría por nombre (case insensitive)
         */
        Optional<CategoryEntity> findByNameIgnoreCase(String name);

}
