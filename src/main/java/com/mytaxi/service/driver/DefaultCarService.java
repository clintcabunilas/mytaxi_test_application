package com.mytaxi.service.driver;

import com.mytaxi.dataaccessobject.CarRepository;
import com.mytaxi.domainobject.CarDO;
import com.mytaxi.exception.ConstraintsViolationException;
import com.mytaxi.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DefaultCarService implements CarService {

    private final CarRepository carRepository;

    public DefaultCarService(final CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    public CarDO find(Long carId) throws EntityNotFoundException {
        return findCarChecked(carId);
    }

    @Override
    public CarDO create(CarDO carDO) throws ConstraintsViolationException {

        try
        {
            return carRepository.save(carDO);
        }
        catch (DataIntegrityViolationException e)
        {
            log.warn("Some constraints are thrown due to driver creation", e);
            throw new ConstraintsViolationException(e.getMessage());
        }
    }

    @Override
    public List<CarDO> getCars() {
        return carRepository.findAll();
    }

    @Override
    public CarDO updateCar(Long id, CarDO newCarDO) throws EntityNotFoundException, ConstraintsViolationException {
        CarDO carDO = findCarChecked(id);

        BeanUtils.copyProperties(newCarDO, carDO);

        return create(carDO);
    }

    @Override
    public void deleteCar(Long id) throws EntityNotFoundException, ConstraintsViolationException {
        CarDO carDO = findCarChecked(id);
        carDO.setDeleted(true);
        create(carDO);
    }

    private CarDO findCarChecked(Long carId) throws EntityNotFoundException {
        return carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Could not find car entity with id: " + carId));
    }
}
