package ec.edu.ups.icc.fundamentos01.categories.mappers;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;

public class CategoryMapper {

    public static CategoryResponseDto toResponseDto(CategoryEntity categoryEntity

    ) {

        return new CategoryResponseDto() {
            {
                id = categoryEntity.getId();
                name = categoryEntity.getName();
                description = categoryEntity.getDescription();
            }
        };

    }

}
