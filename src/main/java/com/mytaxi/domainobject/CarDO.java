package com.mytaxi.domainobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(
        name = "car",
        uniqueConstraints = @UniqueConstraint(name = "license_plate", columnNames = {"license_plate"})
)
@AllArgsConstructor
@NoArgsConstructor
public class CarDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime dateCreated = ZonedDateTime.now();

    @Column(name = "license_plate", nullable = false)
    @NotNull(message = "LicensePlate can not be null!")
    private String licensePlate;

    private Boolean convertible;

    private Float rating;

    @Column(name = "engine_type")
    private String engineType;

    private String manufacturer;

    private boolean deleted;

    public CarDO(String licensePlate, Boolean convertible, Float rating, String engineType, String manufacturer) {
        this.licensePlate = licensePlate;
        this.convertible = convertible;
        this.rating = rating;
        this.engineType = engineType;
        this.manufacturer = manufacturer;
    }
}
