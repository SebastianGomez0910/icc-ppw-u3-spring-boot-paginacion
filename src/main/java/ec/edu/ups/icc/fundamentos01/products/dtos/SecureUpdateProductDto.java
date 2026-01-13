package ec.edu.ups.icc.fundamentos01.products.dtos;

import jakarta.validation.constraints.NotNull;

public class SecureUpdateProductDto {

    public String name;
    public Double price;
    public String description;
    public String reason;

    // ============== RELACIONES ==============

    @NotNull(message = "El ID del usuario es obligatorio")
    public Long userId;

    @NotNull(message = "El ID de la categoría es obligatorio")
    public Long categoryId;

}
