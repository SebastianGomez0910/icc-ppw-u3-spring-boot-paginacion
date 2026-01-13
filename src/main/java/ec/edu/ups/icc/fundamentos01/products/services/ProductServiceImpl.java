package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;
import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.reporitory.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.BusinessException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.SecureUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;

import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.models.Product;
import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public ProductServiceImpl(ProductRepository productRepo, UserRepository userRepo,
            CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepo.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto findOne(Long id) {
        return productRepo.findById((long) id)
                .map(Product::fromEntity)
                .map(ProductMapper::toResponse)
                .orElseThrow(() -> new IllegalStateException("Producto no encontrado"));
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto) {
        // 1. VALIDAR EXISTENCIA DE RELACIONES
        UserEntity owner = userRepo.findById(dto.userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + dto.userId));

        CategoryEntity category = categoryRepo.findById(dto.categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con ID: " + dto.categoryId));

        // Regla: nombre único
        if (productRepo.findByName(dto.name).isPresent()) {
            throw new IllegalStateException("El nombre del producto ya está registrado");
        }

        // 2. CREAR MODELO DE DOMINIO
        Product product = Product.fromDto(dto);

        // 3. CONVERTIR A ENTIDAD CON RELACIONES
        ProductEntity entity = product.toEntity(owner, category);

        // 4. PERSISTIR
        ProductEntity saved = productRepo.save(entity);

        // 5. CONVERTIR A DTO DE RESPUESTA
        return toResponseDto(saved);
    }

    @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto) {
        // 1. BUSCAR PRODUCTO EXISTENTE
        ProductEntity existing = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        // 2. VALIDAR NUEVA CATEGORÍA (si cambió)
        CategoryEntity newCategory = categoryRepo.findById(dto.categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con ID: " + dto.categoryId));

        // 3. ACTUALIZAR USANDO DOMINIO
        Product product = Product.fromEntity(existing);
        product.update(dto);

        // 4. CONVERTIR A ENTIDAD MANTENIENDO OWNER ORIGINAL
        ProductEntity updated = product.toEntity(existing.getOwner(), newCategory);
        updated.setId(id); // Asegurar que mantiene el ID

        // 5. PERSISTIR Y RESPONDER
        ProductEntity saved = productRepo.save(updated);
        return toResponseDto(saved);
    }

    @Override
    public List<ProductResponseDto> findByUserId(Long userId) {

        // Validar que el usuario existe
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + userId);
        }

        return productRepo.findByOwnerId(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<ProductResponseDto> findByCategoryId(Long categoryId) {

        // Validar que la categoría existe
        if (!categoryRepo.existsById(categoryId)) {
            throw new NotFoundException("Categoría no encontrada con ID: " + categoryId);
        }

        return productRepo.findByCategoryId(categoryId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto) {

        // 1. BUSCAR PRODUCTO EXISTENTE
        ProductEntity existing = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        // 2. VALIDAR NUEVA CATEGORÍA (si cambió)
        CategoryEntity newCategory = categoryRepo.findById(existing.getCategory().getId())
                .orElseThrow(() -> new NotFoundException(
                        "Categoría no encontrada con ID: " + existing.getCategory().getId()));

        // 3. ACTUALIZAR USANDO DOMINIO
        Product product = Product.fromEntity(existing);
        product.update(dto);

        // 4. CONVERTIR A ENTIDAD MANTENIENDO OWNER ORIGINAL
        ProductEntity updated = product.toEntity(existing.getOwner(), newCategory);
        updated.setId(id); // Asegurar que mantiene el ID

        // 5. PERSISTIR Y RESPONDER
        ProductEntity saved = productRepo.save(updated);
        return toResponseDto(saved);
    }

    @Override
    public void delete(Long id) {

        // Verifica existencia y elimina
        productRepo.findById((long) id)
                .ifPresentOrElse(
                        productRepo::delete,
                        () -> {
                            throw new IllegalStateException("Producto no encontrado");
                        });
    }

    @Override
    public boolean validateName(Integer id, String name) {
        productRepo.findByName(name)
                .ifPresent(existing -> {
                    if (id == null || existing.getId() != id.longValue()) {
                        throw new ConflictException(
                                "Ya existe un producto con el nombre '" + name + "'");
                    }
                });
        return true;
    }

    @Override
    public ProductResponseDto secureUpdate(int id, SecureUpdateProductDto dto) {

        UserEntity owner = userRepo.findById(dto.userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + dto.userId));

        CategoryEntity category = categoryRepo.findById(dto.categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con ID: " + dto.categoryId));

        ProductEntity entity = productRepo.findById((long) id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        if (dto.price != null && dto.price > 1000) {
            if (dto.reason == null || dto.reason.isBlank()) {
                throw new BusinessException(
                        "Productos con precio mayor a 1000 requieren justificación");
            }
        }

        Product product = Product.fromEntity(entity);

        if (dto.name != null)
            product.setName(dto.name);
        if (dto.price != null)
            product.setPrice(dto.price);
        if (dto.description != null)
            product.setDescription(dto.description);

        ProductEntity saved = productRepo.save(product.toEntity(owner, category));

        return ProductMapper.toResponse(Product.fromEntity(saved));
    }

    /**
     * Convierte ProductEntity a ProductResponseDto
     * Usa estructura anidada para mejor organización semántica
     */
    private ProductResponseDto toResponseDto(ProductEntity entity) {
        ProductResponseDto dto = new ProductResponseDto();

        // Campos básicos del producto
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.price = entity.getPrice();
        dto.description = entity.getDescription();

        // Crear objeto User anidado (se carga LAZY)
        ProductResponseDto.UserSummaryDto userDto = new ProductResponseDto.UserSummaryDto();
        userDto.id = entity.getOwner().getId();
        userDto.name = entity.getOwner().getName();
        userDto.email = entity.getOwner().getEmail();
        dto.user = userDto;

        // Crear objeto Category anidado (se carga LAZY)
        CategoryResponseDto categoryDto = new CategoryResponseDto();
        categoryDto.id = entity.getCategory().getId();
        categoryDto.name = entity.getCategory().getName();
        categoryDto.description = entity.getCategory().getDescription();
        dto.category = categoryDto;

        // Auditoría
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();

        return dto;
    }

}