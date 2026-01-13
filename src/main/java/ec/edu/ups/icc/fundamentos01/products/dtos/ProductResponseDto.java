package ec.edu.ups.icc.fundamentos01.products.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;

@JsonPropertyOrder({
        "id",
        "name",
        "price",
        "description",
        "user",
        "category",
        "createdAt",
        "updatedAt"
})
public class ProductResponseDto {
    public Long id;
    public String name;
    public Double price;
    public String description;

    // ============== OBJETOS ANIDADOS ==============

    public UserSummaryDto user;
    public CategoryResponseDto category;

    // ============== AUDITORÍA ==============

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // ============== DTOs INTERNOS ==============

    public static class UserSummaryDto {
        public Long id;
        public String name;
        public String email;
    }

}
