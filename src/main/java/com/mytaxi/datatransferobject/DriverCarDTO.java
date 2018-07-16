package com.mytaxi.datatransferobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverCarDTO {

    private Long driverId;

    private Long carId;

    private Boolean selected;
}
