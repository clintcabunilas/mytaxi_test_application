package com.mytaxi.service.driver;

import com.mytaxi.domainobject.CarDO;
import com.mytaxi.exception.ConstraintsViolationException;
import com.mytaxi.exception.EntityNotFoundException;

import java.util.List;

public interface CarService {

    CarDO find(Long carId) throws EntityNotFoundException;

    CarDO create(CarDO carDO) throws ConstraintsViolationException;

    List<CarDO> getCars();

    CarDO updateCar(Long id, CarDO carDO) throws EntityNotFoundException, ConstraintsViolationException;

    void deleteCar(Long id) throws EntityNotFoundException, ConstraintsViolationException;
}
