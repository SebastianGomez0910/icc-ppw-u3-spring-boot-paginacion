package ec.edu.ups.icc.fundamentos01.products.mappers;

import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.models.Product;

public class ProductMapper {

    // Agregamos stock aquí
    public static Product toModel(int id, String name, Double price, String description, Integer stock) {
        return new Product(id, name, price, description, stock);
    }

    // DTO -> Model (Entrada: IMPORTANTE)
    public static Product fromCreateDto(CreateProductDto dto) {
        return new Product(dto.name, dto.price, dto.description, dto.stock);
    }

    // DTO -> Model (Entrada: IMPORTANTE)
    public static Product fromUpdateDto(UpdateProductDto dto) {
        return new Product(0, dto.name, dto.price, dto.description, dto.stock);
    }

    // Model -> Response (Salida)
    public static ProductResponseDto toResponse(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.price = product.getPrice();
        dto.description = product.getDescription();
    
        return dto;
    }
}