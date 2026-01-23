package ec.edu.ups.icc.fundamentos01.products.models;

import java.util.Set;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;

public class Product {

    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    private String description;
    private String createdAt;

    // Constructor completo
    public Product(long id, String name, Double price, String description, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.createdAt = java.time.LocalDateTime.now().toString();
    }

    // Constructor para creación (Actualizado para recibir stock)
    public Product(String name, Double price, String description, Integer stock) { 
        this.validateBusinessRules(name, price, description, stock);
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock; 
        this.createdAt = java.time.LocalDateTime.now().toString();
    }

    private void validateBusinessRules(String name, Double price, String description, Integer stock) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (stock == null || stock < 0) { 
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("La descripción no puede superar 500 caracteres");
        }
    }

    // ==================== FACTORY METHODS ====================

    public ProductEntity toEntity(UserEntity owner, Set<CategoryEntity> categories) {
        ProductEntity entity = new ProductEntity();
        if (this.id != null && this.id > 0) {
            entity.setId(this.id);
        }

        entity.setName(this.name);
        entity.setPrice(this.price);
        entity.setDescription(this.description);
        entity.setStock(this.stock); 

        // Asignar relaciones
        entity.setOwner(owner);
        entity.setCategories(categories);

        return entity;
    }

    public Product update(UpdateProductDto dto) {
        this.name = dto.name;
        this.price = dto.price;
        this.description = dto.description;
        this.stock = dto.stock; 
        return this;
    }

    // Método create actualizado
    public static Product fromDto(CreateProductDto dto) {
        return new Product(dto.name, dto.price, dto.description, dto.stock);
    }

    public static Product fromEntity(ProductEntity entity) {
        Product product = new Product(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getDescription(),
                entity.getStock() 
        );
        return product;
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Integer getStock() { return stock; } 
    public void setStock(Integer stock) { this.stock = stock; } 

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Product partialUpdate(PartialUpdateProductDto dto) {
        if (dto.name != null) this.name = dto.name;
        if (dto.price != null) this.price = dto.price;
        if (dto.description != null) this.description = dto.description;
        if (dto.stock != null) this.stock = dto.stock; 
        return this;
    }
}