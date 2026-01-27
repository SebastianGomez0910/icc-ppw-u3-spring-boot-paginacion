package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.reporitory.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;

import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;

import ec.edu.ups.icc.fundamentos01.products.models.Product;
import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

   private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, 
                            UserRepository userRepository,
                            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto, Long userId) {

    UserEntity owner = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado con ID: " + userId));

    Set<CategoryEntity> categories = validateAndGetCategories(dto.categoryIds);

    if (productRepository.findByName(dto.name).isPresent()) {
        throw new IllegalStateException("El nombre del producto ya está registrado");
    }

    Product product = Product.fromDto(dto);


    ProductEntity entity = product.toEntity(owner, categories);

    ProductEntity saved = productRepository.save(entity);

    return toResponseDto(saved);
}

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // ============== MÉTODOS HELPER ==============

    @Override
    public ProductResponseDto findById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));
    }

    @Override
    public List<ProductResponseDto> findByUserId(Long userId) {

        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + userId);
        }

        return productRepository.findByOwnerId(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<ProductResponseDto> findByCategoryId(Long categoryId) {

        // Validar que la categoría existe
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Categoría no encontrada con ID: " + categoryId);
        }

        return productRepository.findByCategoriesId(categoryId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto, UserDetailsImpl currentUser) {

        // 1. BUSCAR PRODUCTO EXISTENTE
        ProductEntity existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        // 2. VALIDAR Y OBTENER CATEGORÍAS
        Set<CategoryEntity> categories = validateAndGetCategories(dto.categoryIds);

        // 3. ACTUALIZAR USANDO DOMINIO
        Product product = Product.fromEntity(existing);
        product.update(dto);

        // 4. CONVERTIR A ENTIDAD MANTENIENDO OWNER ORIGINAL
        ProductEntity updated = product.toEntity(existing.getOwner(), categories);
        updated.setId(id); // Asegurar que mantiene el ID

        // 5. PERSISTIR Y RESPONDER
        ProductEntity saved = productRepository.save(updated);
        return toResponseDto(saved);
    }

    @Override
    public void delete(Long id, UserDetailsImpl currentUser) {

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        validateOwnership(product, currentUser);
        // Eliminación física (también se puede implementar lógica)
        productRepository.delete(product);
    }

    private void validateOwnership(ProductEntity product, UserDetailsImpl currentUser) {
    // Si es ADMIN o MODERATOR, saltar validación
    if (hasAnyRole(currentUser, "ROLE_ADMIN", "ROLE_MODERATOR")) {
        return; 
    }

    // Si es USER, comparar IDs
    if (!product.getOwner().getId().equals(currentUser.getId())) {
        throw new AccessDeniedException("No puedes modificar productos ajenos");
    }
}

private boolean hasAnyRole(UserDetailsImpl user, String... roles) {
    for (String role : roles) {
        for (GrantedAuthority auth : user.getAuthorities()) {
            if (auth.getAuthority().equals(role)) return true;
        }
    }
    return false;
}


    private ProductResponseDto toResponseDto(ProductEntity entity) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.price = entity.getPrice();
        dto.description = entity.getDescription();

        dto.createdAt = entity.getCreatedAt(); 
        dto.updatedAt = entity.getUpdatedAt();

        ProductResponseDto.UserSummaryDto ownerDto = new ProductResponseDto.UserSummaryDto();
        ownerDto.id = entity.getOwner().getId();
        ownerDto.name = entity.getOwner().getName();
        ownerDto.email = entity.getOwner().getEmail();

        List<CategoryResponseDto> categoryDtos = new ArrayList<>();
        for (CategoryEntity categoryEntity : entity.getCategories()) {
            CategoryResponseDto categoryDto = new CategoryResponseDto();
            categoryDto.id = categoryEntity.getId();
            categoryDto.name = categoryEntity.getName();

            categoryDtos.add(categoryDto);
        }
        dto.user = ownerDto;
        dto.categories = categoryDtos;
        return dto;

    }

    private Set<CategoryEntity> validateAndGetCategories(Set<Long> categoryIds) {
        Set<CategoryEntity> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {
            CategoryEntity category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + categoryId));
            categories.add(category);
        }

        return categories;
    }

    @Override
    public Page<ProductResponseDto> findAllPaginado(int page, int size, String[] sort) {
       Pageable pageable = createPageable(page, size, sort);
        Page<ProductEntity> productPage = productRepository.findAll(pageable);
        
        return productPage.map(this::toResponseDto);
    }

    private static final int MAX_PAGE_LIMIT = 1000;

    private Pageable createPageable(int page, int size, String[] sort) {
        // Validar parámetros
        if (page < 0) {
            throw new BadRequestException("La página debe ser mayor o igual a 0");
        }
        if(page > MAX_PAGE_LIMIT){
            throw new BadRequestException("No se permite solicitar mas alla de la pagina "+ MAX_PAGE_LIMIT);
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("El tamaño debe estar entre 1 y 100");
        }
        
        // Crear Sort
        Sort sortObj = createSort(sort);
        
        return PageRequest.of(page, size, sortObj);
    }

      private Sort createSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.by("id");
        }

        if (sort.length == 2 && (sort[1].equalsIgnoreCase("asc") || sort[1].equalsIgnoreCase("desc"))) {
             String property = sort[0];
             String direction = sort[1];
             
             if (!isValidSortProperty(property)) {
                 throw new BadRequestException("Propiedad de ordenamiento no válida: " + property);
             }
             
             return direction.equalsIgnoreCase("desc") ? 
                    Sort.by(Sort.Order.desc(property)) : 
                    Sort.by(Sort.Order.asc(property));
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        
        for (String sortParam : sort) {
            String[] parts = sortParam.split(",");
            String property = parts[0];
            String direction = parts.length > 1 ? parts[1] : "asc";

            if (!isValidSortProperty(property)) {
                throw new BadRequestException("Propiedad de ordenamiento no válida: " + property);
            }

            orders.add(direction.equalsIgnoreCase("desc") ? 
                       Sort.Order.desc(property) : 
                       Sort.Order.asc(property));
        }
        
        return Sort.by(orders);
    }

    private boolean isValidSortProperty(String property) {
        // Lista blanca de propiedades permitidas para ordenamiento
        Set<String> allowedProperties = Set.of(
            "id", "name", "price", "createdAt", "updatedAt",
            "owner.name", "owner.email", "category.name"
        );
        return allowedProperties.contains(property);
    }

    private void validateFilterParameters(Double minPrice, Double maxPrice) {
        if (minPrice != null && minPrice < 0) {
            throw new BadRequestException("El precio mínimo no puede ser negativo");
        }
        
        if (maxPrice != null && maxPrice < 0) {
            throw new BadRequestException("El precio máximo no puede ser negativo");
        }
        
        if (minPrice != null && maxPrice != null && maxPrice < minPrice) {
            throw new BadRequestException("El precio máximo debe ser mayor o igual al precio mínimo");
        }
    }

    @Override
    public Slice<ProductResponseDto> findAllSlice(int page, int size, String[] sort) {
        Pageable pageable = createPageable(page, size, sort);
        return productRepository.findAllBy(pageable)
            .map(this::toResponseDto);
    }

    @Override
    public Page<ProductResponseDto> findWithFilters(String name, Double minPrice, Double maxPrice, Long categoryId,
            int page, int size, String[] sort) {
        validateFilterParameters(minPrice, maxPrice);

        Pageable pageable = createPageable(page, size, sort);

        return productRepository.findWithFilters(name, minPrice, maxPrice, categoryId, pageable)
                .map(this::toResponseDto);
    }

@Override
    public Page<ProductResponseDto> findByUserIdWithFilters(Long userId, String name, Double minPrice, Double maxPrice,
            Long categoryId, int page, int size, String[] sort) {
        
        validateFilterParameters(minPrice, maxPrice);
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<ProductEntity> productPage = productRepository.findByUserIdWithFilters(
            userId, name, minPrice, maxPrice, categoryId, pageable);
        
        return productPage.map(this::toResponseDto);
    }
}