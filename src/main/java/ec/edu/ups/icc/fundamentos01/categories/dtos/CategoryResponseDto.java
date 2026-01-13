package ec.edu.ups.icc.fundamentos01.categories.dtos;

import java.util.List;

import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;

public class CategoryResponseDto {

    public Long id;
    public String name;
    public String description;

    public List<ProductResponseDto> products;
}
