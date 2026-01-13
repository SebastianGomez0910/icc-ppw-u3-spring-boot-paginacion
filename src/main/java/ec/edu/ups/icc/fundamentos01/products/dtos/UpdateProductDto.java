package ec.edu.ups.icc.fundamentos01.products.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdateProductDto {

    @NotBlank
    @Size(min = 3, max = 150)
    public String name;

    @NotNull
    @Positive
    public Double price;

    @Size(max = 500)
    public String description;

    // ============== ACTUALIZACIÓN DE RELACIONES ==============

    @NotNull(message = "El ID de la categoría es obligatorio")
    public Long categoryId;

    // Nota: No se permite cambiar el owner de un producto una vez creado
    // Si fuera necesario, sería una operación de negocio especial
}
