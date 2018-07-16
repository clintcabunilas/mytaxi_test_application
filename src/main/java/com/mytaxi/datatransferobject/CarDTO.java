package com.mytaxi.datatransferobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarDTO {

    private Long id;

    private String licensePlate;

    private Boolean convertible;

    private Float rating;

    private String engine;

    private String manufacturer;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean deleted;
}
