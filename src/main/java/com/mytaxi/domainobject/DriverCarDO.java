package com.mytaxi.domainobject;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(
        name = "driver_car",
        uniqueConstraints = @UniqueConstraint(name = "car_id_driver_id", columnNames = {"car_id", "driver_id"})
)
public class DriverCarDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private CarDO carDO;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverDO driverDO;

    @Column(name = "selected", nullable = false)
    private Boolean selected = false;

}
