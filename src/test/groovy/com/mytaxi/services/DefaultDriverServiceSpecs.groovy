package com.mytaxi.services

import com.mytaxi.dataaccessobject.DriverRepository
import com.mytaxi.domainobject.CarDO
import com.mytaxi.domainobject.DriverCarDO
import com.mytaxi.domainobject.DriverDO
import com.mytaxi.domainvalue.GeoCoordinate
import com.mytaxi.domainvalue.OnlineStatus
import com.mytaxi.exception.ConstraintsViolationException
import com.mytaxi.exception.EntityNotFoundException
import com.mytaxi.service.driver.CarDriverService
import com.mytaxi.service.driver.DefaultDriverService
import com.mytaxi.service.driver.DriverService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import spock.lang.Specification

class DefaultDriverServiceSpecs extends Specification {

    DriverRepository driverRepository = Mock(DriverRepository.class)

    CarDriverService carDriverService = Mock(CarDriverService.class)

    DriverService driverService = new DefaultDriverService(driverRepository, carDriverService)

    def "Should return an existing driver"() {
        def id = 1L
        driverRepository.findById(id) >> createOptionalDriver(id)

        when:
        DriverDO driverDO = driverService.find(id)

        then:
        driverDO.getId() == id
    }

    def "Should throw EntityNotFoundException when finding a non-existent driver"() {
        def id = 1L
        driverRepository.findById(id) >> Optional.empty()

        when:
        driverService.find(id)

        then:
        thrown(EntityNotFoundException.class)
    }

    def "Should throw ConstraintsViolationException when creating a driver with Data Integrity Violation"() {
        def id = 1L
        def newDriver = createNewDriver(id)
        driverRepository.save(newDriver) >> { throw new DataIntegrityViolationException("message")}

        when:
        driverService.create(newDriver)

        then:
        thrown(ConstraintsViolationException.class)
    }

    def "Should create new driver"() {
        def id = 1L
        def newDriver = createNewDriver(id)
        driverRepository.save(newDriver) >> newDriver

        when:
        DriverDO driverDO = driverService.create(newDriver)

        then:
        driverDO.equals(newDriver)
    }

    def "Should throw EntityNotFoundException when deleting non-existent driver"() {
        def id = 1L
        driverRepository.findById(id) >> Optional.empty()

        when: "deleting a non-existent driver"
        driverService.delete(id)

        then: "throw EntityNotFoundException"
        thrown(EntityNotFoundException.class)
    }

    def "Should delete a driver"() {
        def id = 1L
        def optionalDriver = createOptionalDriver(id)
        def deletedDriver = optionalDriver.get()
        deletedDriver.deleted = true

        driverRepository.findById(id) >> optionalDriver

        when: "deleting a driver"
        driverService.delete(id)

        then: "driver exists"
        1 * driverRepository.findById(id) >> optionalDriver
        0 * _._
    }

    def "Should update driver location"() {
        def id = 1L
        def driver = createNewDriver(1)
        def driverLocation = new GeoCoordinate(90, 90)
        def driverWithNewLocation = createNewDriver(id)
        driverWithNewLocation.coordinate = driverLocation

        driverRepository.findById(id) >> Optional.of(driver)

        when: "updating a driver location"
        driverService.updateLocation(id, 90, 90)

        then: "driver should exist"
        1 * driverRepository.findById(id) >> Optional.of(driver)
        0 * _._
    }

    def "Should return driver by onlineStatus = OFFLINE"() {
        driverRepository.findByOnlineStatus(OnlineStatus.OFFLINE) >> createOfflineDrivers()

        when: "getting drivers with offline status"
        List<DriverDO> drivers = driverService.find(OnlineStatus.OFFLINE)

        then: "return drivers with offline status"
        for(DriverDO driverDO : drivers) {
            driverDO.onlineStatus == OnlineStatus.OFFLINE
        }
    }

    def "Should return driver by onlineStatus = ONLINE"() {
        driverRepository.findByOnlineStatus(OnlineStatus.ONLINE) >> createOnlineDrivers()

        when: "getting drivers with ONLINE status"
        List<DriverDO> drivers = driverService.find(OnlineStatus.ONLINE)

        then: "return drivers with ONLINE status"
        for(DriverDO driverDO : drivers) {
            driverDO.onlineStatus == OnlineStatus.ONLINE
        }
    }

    def "Should return all drivers with query param"() {
        Pageable pageable = new PageRequest(0, 10)
        def drivers = new PageImpl<DriverCarDO>(createCarDrivers())
        def queryParam = [username: "username", licensePlate: "licensePlate"]
        carDriverService.findCarDrivers(queryParam, pageable) >> drivers

        when: "getting drivers with no query param"
        Page<DriverDO> driversPage = driverService.getDrivers(queryParam, pageable)

        then: "return drivers"
        for(DriverDO driversDO : driversPage.content) {
            driversDO.id.equals(drivers.content.driverDO.id)
        }
    }

    List<DriverCarDO> createCarDrivers() {
        return [new DriverCarDO(id:1, driverDO: createNewDriver(1), carDO: createNewCar(1)),
                new DriverCarDO(id:2, driverDO: createNewDriver(2), carDO: createNewCar(2)),
                new DriverCarDO(id:3, driverDO: createNewDriver(2), carDO: createNewCar(3))] as List
    }

    CarDO createNewCar(Long id) {
        return new CarDO(id: id, licensePlate: "licensePlate"+1)
    }

    List<DriverDO> createDrivers() {
        return [new DriverDO(id: 1, username: "username1", password: "pwd1", onlineStatus: OnlineStatus.OFFLINE),
                new DriverDO(id: 2, username: "username2", password: "pwd2", onlineStatus: OnlineStatus.ONLINE),
                new DriverDO(id: 3, username: "username3", password: "pwd3", onlineStatus: OnlineStatus.OFFLINE)] as List
    }

    List<DriverDO> createOfflineDrivers() {
        return [new DriverDO(id: 1, username: "username1", password: "pwd1", onlineStatus: OnlineStatus.OFFLINE),
                new DriverDO(id: 2, username: "username2", password: "pwd2", onlineStatus: OnlineStatus.OFFLINE),
                new DriverDO(id: 3, username: "username3", password: "pwd3", onlineStatus: OnlineStatus.OFFLINE)] as List
    }

    List<DriverDO> createOnlineDrivers() {
        return [new DriverDO(id: 1, username: "username1", password: "pwd1", onlineStatus: OnlineStatus.ONLINE),
                new DriverDO(id: 2, username: "username2", password: "pwd2", onlineStatus: OnlineStatus.ONLINE),
                new DriverDO(id: 3, username: "username3", password: "pwd3", onlineStatus: OnlineStatus.ONLINE)] as List
    }

    DriverDO createNewDriver(Long id) {
        return new DriverDO(id: id, username: "username", password: "pwd", onlineStatus: OnlineStatus.ONLINE)
    }

    Optional<DriverDO> createOptionalDriver(Long id) {
        return Optional.of(new DriverDO(id: id))
    }
}
