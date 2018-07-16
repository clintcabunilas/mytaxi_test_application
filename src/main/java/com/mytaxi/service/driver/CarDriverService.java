package com.mytaxi.service.driver;

import com.mytaxi.domainobject.DriverCarDO;
import com.mytaxi.exception.CarAlreadyInUseException;
import com.mytaxi.exception.ConstraintsViolationException;
import com.mytaxi.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface CarDriverService {

    DriverCarDO selectCarForDriver(Long driverId, Long carId) throws EntityNotFoundException, CarAlreadyInUseException, ConstraintsViolationException;

    DriverCarDO deselectCarForDriver(Long driverId, Long carId) throws EntityNotFoundException, CarAlreadyInUseException, ConstraintsViolationException;

    DriverCarDO find(Long driverId, Long carId);

    Page<DriverCarDO> findCarDrivers(Map<String, Object> allRequestParams, Pageable pageable);
}
