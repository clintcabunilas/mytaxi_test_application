package com.mytaxi.controller;

import com.mytaxi.controller.mapper.CarMapper;
import com.mytaxi.datatransferobject.CarDTO;
import com.mytaxi.domainobject.CarDO;
import com.mytaxi.exception.ConstraintsViolationException;
import com.mytaxi.exception.EntityNotFoundException;
import com.mytaxi.service.driver.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * All operations with a driver will be routed by this controller.
 * <p/>
 */
@RestController
@RequestMapping("v1/cars")
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(CarService carService)
    {
        this.carService = carService;
    }

    @PostMapping
    public CarDTO createCar(@Valid @RequestBody CarDTO carDTO) throws ConstraintsViolationException {
        CarDO carDO = CarMapper.makeCarDO(carDTO);
        return CarMapper.makeCarDTO(carService.create(carDO));
    }

    @GetMapping
    public List<CarDTO> getCars() {
        return CarMapper.makeCarDTOList(carService.getCars());
    }

    @GetMapping("/{id}")
    public CarDTO getCar(@PathVariable(value = "id") Long id) throws EntityNotFoundException {
        return CarMapper.makeCarDTO(carService.find(id));
    }

    @PutMapping("/{id}")
    public CarDTO updateCar(@PathVariable(value = "id") Long id, @RequestBody CarDTO carDTO) throws EntityNotFoundException, ConstraintsViolationException {
        CarDTO carDTOWithId = carDTO;
        carDTOWithId.setId(id);
        return CarMapper.makeCarDTO(carService.updateCar(id, CarMapper.makeCarDO(carDTOWithId)));
    }

    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable(value = "id") Long id) throws EntityNotFoundException, ConstraintsViolationException {
        carService.deleteCar(id);
    }
}
