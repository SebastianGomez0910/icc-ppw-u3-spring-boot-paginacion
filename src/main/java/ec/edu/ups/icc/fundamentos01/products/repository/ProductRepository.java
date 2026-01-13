package ec.edu.ups.icc.fundamentos01.products.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByName(String name);

    List<ProductEntity> findByOwnerId(Long userId);

    List<ProductEntity> findByCategoryId(Long categoryId);

    /**
     * Encuentra productos por nombre de usuario
     * Genera JOIN automáticamente:
     * SELECT p.* FROM products p JOIN users u ON p.user_id = u.id WHERE u.name = ?
     */
    List<ProductEntity> findByOwnerName(String ownerName);

    /**
     * Encuentra productos por nombre de categoría
     * Genera JOIN automáticamente:
     * SELECT p.* FROM products p JOIN categories c ON p.category_id = c.id WHERE
     * c.name = ?
     */
    List<ProductEntity> findByCategoryName(String categoryName);

    /**
     * Encuentra productos con precio mayor a X de una categoría específica
     * Consulta con múltiples condiciones
     * Genera:
     * SELECT p.* FROM products p WHERE p.category_id = ? AND p.price > ?
     */
    List<ProductEntity> findByCategoryIdAndPriceGreaterThan(Long categoryId, Double price);

}
