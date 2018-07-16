package com.mytaxi.controller.mapper;

import com.mytaxi.datatransferobject.DriverCarDTO;
import com.mytaxi.domainobject.DriverCarDO;

public class DriverCarMapper {

    public static DriverCarDTO makeDriverCarDTO(DriverCarDO driverCarDO) {
        return new DriverCarDTO(driverCarDO.getDriverDO().getId(), driverCarDO.getCarDO().getId(), driverCarDO.getSelected());
    }
}
