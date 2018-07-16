package com.mytaxi.services

import com.mytaxi.dataaccessobject.CarRepository
import com.mytaxi.domainobject.CarDO
import com.mytaxi.exception.ConstraintsViolationException
import com.mytaxi.exception.EntityNotFoundException
import com.mytaxi.service.driver.CarService
import com.mytaxi.service.driver.DefaultCarService
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

class DefaultCarServiceSpec extends Specification {

    CarRepository carRepository = Mock(CarRepository.class)

    CarService carService = new DefaultCarService(carRepository)

    def "Should return car by id"() {
        def id = 1L
        carRepository.findById(id) >> createOptionalCar(id)

        when: "finding a car by id"
        CarDO car = carService.find(id)

        then: "return a car with the given id"
        car.getId() == id
    }

    def "Should throw EntityNotFoundException when getting a non-existent car"() {
        def id = 1L
        carRepository.findById(id) >> Optional.empty()

        when: "finding a non-existent car by id"
        carService.find(id)

        then: "throw EntityNotFoundException"
        thrown(EntityNotFoundException.class)
    }

    def "Should create a new car"() {
        def carDO = createCar()
        carRepository.save(carDO) >> carDO

        when: "creating a car"
        def newCarDO = carService.create(carDO)

        then: "return the newly created car"
        newCarDO == carDO
    }

    def "Should throw ConstraintsViolationException when creating a new car"() {
        def carDO = createCar()
        carRepository.save(carDO) >> { throw new DataIntegrityViolationException("message") }

        when: "creating a car with Data Integrity Violation"
        def newCarDO = carService.create(carDO)

        then: "throw ConstraintsViolationException"
        thrown(ConstraintsViolationException.class)
    }

    def "Should return all cars"() {
        def carDOList = createCars()
        carRepository.findAll() >> carDOList

        when: "getting all cars"
        def carDOListFromDB = carService.getCars()

        then: "return all cars"
        carDOList == carDOListFromDB
    }

    def "Should throw EntityNotFoundException when updating a non-existent car"() {
        def id = 1L
        def carDO = createCar()
        carRepository.findById(id) >> Optional.empty()

        when: "updating a non-existent car"
        carService.updateCar(id, carDO)

        then: "throw EntityNotFoundException"
        thrown(EntityNotFoundException.class)
    }

    def "Should update a car"() {
        def id = 1L
        def updatedCarDO = createCar()
        updatedCarDO.manufacturer = "Updated Manufacturer"

        carRepository.findById(id) >> createOptionalCar(id)
        carRepository.save(updatedCarDO) >> updatedCarDO

        when: "updating a car"
        CarDO carDO = carService.updateCar(id, updatedCarDO)

        then: "return an updated car"
        carDO == updatedCarDO
    }

    def "Should throw ConstraintViolationException when updating a car with Data Integrity Violation"() {
        def id = 1L
        def updatedCarDO = updatedCar()

        carRepository.findById(id) >> createOptionalCar(id)
        carRepository.save(updatedCarDO) >> { throw new DataIntegrityViolationException("message") }

        when: "updating a car with data integrity violation"
        carService.updateCar(id, updatedCarDO)

        then: "throw ConstraintViolationException"
        thrown(ConstraintsViolationException.class)
    }

    def "Should throw EntityNotFoundException when deleting a non-existent car"() {
        def id = 1L

        carRepository.findById(id) >> Optional.empty()

        when: "deleting a non-exitent car"
        carService.deleteCar(id)

        then: "throw ConstraintViolationException"
        thrown(EntityNotFoundException.class)
    }

    def "Should delete a car"() {
        def id = 1L
        def optionalCar = createOptionalCar(id)
        def deletedCar = optionalCar.get()
        deletedCar.deleted = true

        carRepository.findById(id) >> optionalCar
        carRepository.save(deletedCar) >> deletedCar

        when: "deleting a car"
        carService.deleteCar(id)

        then: "car exists and car is saved as deleted"
        1 * carRepository.findById(id) >> optionalCar
        1 * carRepository.save(deletedCar) >> deletedCar
        0 * _._
    }

    CarDO createDeletedCar(id) {
        CarDO carDO = new CarDO(id: id, deleted: true, convertible: true,  dateCreated: ZonedDateTime.of(2018, 07, 12, 11, 30, 0, 0, ZoneId.of("Z")))
        return carDO
    }

    CarDO updatedCar() {
        CarDO carDO = createCar()
        carDO.manufacturer = "Updated Manufacturer"
        return carDO
    }

    List<CarDO> createCars() {
        return [new CarDO(id: 1, licensePlate: "ABC123", convertible: false, rating: 5.0, engineType: "gas", manufacturer: "MNF1", deleted: false),
                new CarDO(id: 2, licensePlate: "DEF123", convertible: true, rating: 4.5, engineType: "electric", manufacturer: "MNF2", deleted: true),
                new CarDO(id: 3, licensePlate: "GHI123", convertible: true, rating: 3.5, engineType: "diesel", manufacturer: "MNF1", deleted: true)] as List
    }

    CarDO createCar() {
        return new CarDO(id: 1, licensePlate: "ABC123", convertible: false, rating: 5.0, engineType: "gas", manufacturer: "MNF1", deleted: false)
    }

    Optional<CarDO> createOptionalCar(Long id) {
        return Optional.of(new CarDO(id: id))
    }
}