package com.mytaxi.services

import com.mytaxi.dataaccessobject.CarDriverRepository
import com.mytaxi.dataaccessobject.DriverRepository
import com.mytaxi.domainobject.CarDO
import com.mytaxi.domainobject.DriverCarDO
import com.mytaxi.domainobject.DriverDO
import com.mytaxi.domainvalue.OnlineStatus
import com.mytaxi.exception.CarAlreadyInUseException
import com.mytaxi.exception.EntityNotFoundException
import com.mytaxi.service.driver.CarDriverService
import com.mytaxi.service.driver.CarService
import com.mytaxi.service.driver.DefaultCarDriverService
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

class DefaultCarDriverServiceSpecs extends Specification {

    CarDriverRepository carDriverRepository = Mock(CarDriverRepository.class)

    CarService carService = Mock(CarService.class)

    DriverRepository driverRepository = Mock(DriverRepository.class)

    CarDriverService carDriverService = new DefaultCarDriverService(carDriverRepository, carService, driverRepository)

    def "Should throw NoEntityFoundException if a driver selecting is non-existent"() {
        def driverId = 1L
        def carId = 1L
        driverRepository.findById(driverId) >> Optional.empty()

        when: "a driver is selecting is non-existent"
        carDriverService.selectCarForDriver(driverId, carId)

        then: "throw a EntityNotFoundException"
        thrown(EntityNotFoundException.class)
    }

    def "Should throw NoEntityFoundException if a driver is selecting a non-existent car"() {
        def driverId = 1L
        def carId = 1L
        def driver = createDriver(driverId)
        driverRepository.findById(driverId) >> Optional.of(driver)
        carService.find(carId) >> {throw new EntityNotFoundException("message")}

        when: "a driver is selecting a non-existent car"
        carDriverService.selectCarForDriver(driverId, carId)

        then: "throw a EntityNotFoundException"
        thrown(EntityNotFoundException.class)
    }

    def "Should throw IllegalStateException if an offline driver has the car selected when a new driver is selecting the car"() {
        def driverId = 1L
        def carId = 1L
        def driver = createDriver(driverId)
        driverRepository.findById(driverId) >> Optional.of(driver)
        def car = createCar(carId)
        carService.find(carId) >> createCar(car.id)
        carDriverRepository.findByCarDO_IdAndSelectedIsTrue(car.id) >> createCarDrivers()

        when: "a driver is selecting a car which is selected previously by now an offline driver"
        carDriverService.selectCarForDriver(driverId, carId)

        then: "throw an IllegalStateException"
        thrown(IllegalStateException.class)
    }

    def "Should select a car successfully when a driver selects a car for the first time"() {
        def driverId = 1L
        def carId = 1L
        def optionalDriver = createOptionalDriver(driverId)
        def driver = optionalDriver.get()

        driverRepository.findById(driverId) >> optionalDriver

        def car = createCar(carId)
        carService.find(carId) >> car
        carDriverRepository.findByCarDO_IdAndSelectedIsTrue(car.id) >> null

        carDriverRepository.findByDriverDO_IdAndCarDO_Id(driver.id, car.id) >> null

        DriverCarDO driverCarDO = new DriverCarDO(carDO: car, driverDO: driver)
        driverCarDO.selected = true
        carDriverRepository.save(driverCarDO) >> driverCarDO

        when: "a driver is selecting a car for the first time"
        DriverCarDO driverCarDOSelected = carDriverService.selectCarForDriver(driverId, carId)

        then: "a car is selected by a driver successfully"
        2 * driverRepository.findById(driverId) >> optionalDriver
        2 * carService.find(carId) >> car
        1 * carDriverRepository.findByCarDO_IdAndSelectedIsTrue(car.id) >> null
        1 * carDriverRepository.findByDriverDO_IdAndCarDO_Id(driver.id, car.id) >> null
        1 * carDriverRepository.save(driverCarDO) >> driverCarDO
        0 * _._

        driverCarDOSelected.driverDO.id == driverId
        driverCarDOSelected.carDO.id == carId
        driverCarDOSelected.selected
    }

    def "Should throw CarAlreadyInUsedException"() {
        def driverId = 1L
        def carId = 1L
        def optionalDriver = createOptionalDriver(driverId)
        def driver = optionalDriver.get()

        driverRepository.findById(driverId) >> optionalDriver

        def car = createCar(carId)
        carService.find(carId) >> car

        def id = 1L
        def carDriver = createSelectedCarDriver(id, car, driver)
        carDriver.driverDO.id = 2

        carDriverRepository.findByCarDO_IdAndSelectedIsTrue(car.id) >> ([carDriver] as List)

        when: "a driver is selecting a car that is selected by an online driver"
        carDriverService.selectCarForDriver(driverId, carId)

        then: "throw CarAlreadyInUsedException"
        thrown(CarAlreadyInUseException.class)
    }

    def "Should select successfully when a driver selects a car he had previously deselected"() {
        def driverId = 1L
        def carId = 1L
        def optionalDriver = createOptionalDriver(driverId)
        def driver = optionalDriver.get()

        driverRepository.findById(driverId) >> optionalDriver

        def car = createCar(carId)
        carService.find(carId) >> car
        carDriverRepository.findByCarDO_IdAndSelectedIsTrue(car.id) >> null

        def id = 1L
        def carDriver = createDeselectedCarDriver(id, car, driver)
        carDriverRepository.findByDriverDO_IdAndCarDO_Id(driver.id, car.id) >> carDriver


        DriverCarDO driverCarDO = new DriverCarDO(carDO: car, driverDO: driver)
        driverCarDO.selected = true

        carDriverRepository.save(driverCarDO) >> driverCarDO

        when: "a driver is selecting a car which he/she selected previously"
        DriverCarDO driverCarDOSelected = carDriverService.selectCarForDriver(driverId, carId)

        then: "a car is selected by a driver successfully"
        2 * driverRepository.findById(driverId) >> optionalDriver
        2 * carService.find(carId) >> car
        1 * carDriverRepository.findByCarDO_IdAndSelectedIsTrue(car.id) >> null
        1 * carDriverRepository.findByDriverDO_IdAndCarDO_Id(driver.id, car.id) >> null
        1 * carDriverRepository.save(driverCarDO) >> driverCarDO
        0 * _._

        driverCarDOSelected.driverDO.id == driverId
        driverCarDOSelected.carDO.id == carId
        driverCarDOSelected.selected
    }

    def "Should throw NoEntityFoundException if a driver deselecting is non-existent"() {
        def driverId = 1L
        def carId = 1L
        driverRepository.findById(driverId) >> Optional.empty()

        when: "a driver is selecting is non-existent"
        carDriverService.deselectCarForDriver(driverId, carId)

        then: "throw a EntityNotFoundException"
        thrown(EntityNotFoundException.class)
    }

    def "Should throw NoEntityFoundException if a driver is deselecting a non-existent car"() {
        def driverId = 1L
        def carId = 1L
        def driver = createDriver(driverId)
        driverRepository.findById(driverId) >> Optional.of(driver)
        carService.find(carId) >> {throw new EntityNotFoundException("message")}

        when: "a driver is deselecting a non-existent car"
        carDriverService.deselectCarForDriver(driverId, carId)

        then: "throw a EntityNotFoundException"
        thrown(EntityNotFoundException.class)
    }

    def "Should throw EntityNotFoundException if a driver is deselecting a car he has not selected yet"() {
        def driverId = 1L
        def carId = 1L
        def optionalDriver = createOptionalDriver(driverId)
        def driver = optionalDriver.get()

        driverRepository.findById(driverId) >> optionalDriver

        def car = createCar(carId)
        carService.find(carId) >> car
        carDriverRepository.findByCarDO_IdAndSelectedIsTrue(car.id) >> null

        carDriverRepository.findByDriverDO_IdAndCarDO_Id(driver.id, car.id) >> null

        when: "a driver is deselecting a car he has not selected yet"
        carDriverService.deselectCarForDriver(driverId, carId)

        then: "a car is deselected by a driver successfully"
        thrown(EntityNotFoundException.class)
    }

    def "Should deselect successfully when a driver selects a car he had previously selected"() {
        def driverId = 1L
        def carId = 1L
        def optionalDriver = createOptionalDriver(driverId)
        def driver = optionalDriver.get()

        driverRepository.findById(driverId) >> optionalDriver

        def car = createCar(carId)
        carService.find(carId) >> car

        def selectedCarDriverDO = createSelectedCarDriver(1, car, driver)
        carDriverRepository.findByCarDO_IdAndSelectedIsTrue(car.id) >> ([selectedCarDriverDO] as List)

        def id = 1L
        def carDriver = createDeselectedCarDriver(id, car, driver)

        carDriverRepository.save(carDriver) >> carDriver

        when: "a driver is deselecting a car which he/she selected previously"
        DriverCarDO driverCarDOSelected = carDriverService.deselectCarForDriver(driverId, carId)

        then: "a car is deselected by a driver successfully"
        driverCarDOSelected.driverDO.id == driverId
        driverCarDOSelected.carDO.id == carId
        !driverCarDOSelected.selected
    }

    Optional<DriverDO> createOptionalDriver(Long id) {
        return Optional.of(new DriverDO(id: id))
    }

    DriverCarDO createDeselectedCarDriver(Long id, CarDO car, DriverDO driver) {
        return new DriverCarDO(id: id, carDO:  car, driverDO: driver, selected: false)
    }

    DriverCarDO createSelectedCarDriver(Long id, CarDO car, DriverDO driver) {
        return new DriverCarDO(id: id, carDO:  car, driverDO: driver, selected: true)
    }

    List<DriverCarDO> createCarDrivers() {
        return [new DriverCarDO(id:1, driverDO: createDriver(1), carDO: createCar(1)),
                new DriverCarDO(id:2, driverDO: createDriver(2), carDO: createCar(2)),
                new DriverCarDO(id:3, driverDO: createDriver(2), carDO: createCar(3))] as List
    }

    CarDO createCar(Long id) {
        return new CarDO(id: id, licensePlate: "ABC123", convertible: false, rating: 5.0, engineType: "gas", manufacturer: "MNF1", deleted: false, dateCreated: ZonedDateTime.of(2018, 07, 12, 11, 30, 0, 0, ZoneId.of("Z")))
    }

    DriverDO createDriver(Long id) {
        return new DriverDO(id: id, username: "username", password: "pwd", onlineStatus: OnlineStatus.ONLINE)
    }
}
