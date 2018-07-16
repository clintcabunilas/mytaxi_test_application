package com.mytaxi.controller;

import com.mytaxi.controller.mapper.DriverCarMapper;
import com.mytaxi.controller.mapper.DriverMapper;
import com.mytaxi.datatransferobject.DriverCarDTO;
import com.mytaxi.datatransferobject.DriverDTO;
import com.mytaxi.domainobject.DriverDO;
import com.mytaxi.domainvalue.OnlineStatus;
import com.mytaxi.exception.CarAlreadyInUseException;
import com.mytaxi.exception.ConstraintsViolationException;
import com.mytaxi.exception.EntityNotFoundException;
import com.mytaxi.service.driver.CarDriverService;
import com.mytaxi.service.driver.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * All operations with a driver will be routed by this controller.
 * <p/>
 */
@RestController
@RequestMapping("v1/drivers")
public class DriverController
{

    private final DriverService driverService;

    private final CarDriverService carDriverService;


    @Autowired
    public DriverController(final DriverService driverService, CarDriverService carDriverService)
    {
        this.driverService = driverService;
        this.carDriverService = carDriverService;
    }


    @GetMapping("/{driverId}")
    public DriverDTO getDriver(@Valid @PathVariable long driverId) throws EntityNotFoundException
    {
        return DriverMapper.makeDriverDTO(driverService.find(driverId));
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverDTO createDriver(@Valid @RequestBody DriverDTO driverDTO) throws ConstraintsViolationException
    {
        DriverDO driverDO = DriverMapper.makeDriverDO(driverDTO);
        return DriverMapper.makeDriverDTO(driverService.create(driverDO));
    }


    @DeleteMapping("/{driverId}")
    public void deleteDriver(@Valid @PathVariable long driverId) throws EntityNotFoundException
    {
        driverService.delete(driverId);
    }


    @PutMapping("/{driverId}")
    public void updateLocation(
        @Valid @PathVariable long driverId, @RequestParam double longitude, @RequestParam double latitude)
        throws ConstraintsViolationException, EntityNotFoundException
    {
        driverService.updateLocation(driverId, longitude, latitude);
    }


    @GetMapping
    public List<DriverDTO> findDrivers(@RequestParam OnlineStatus onlineStatus)
        throws ConstraintsViolationException, EntityNotFoundException
    {
        return DriverMapper.makeDriverDTOList(driverService.find(onlineStatus));
    }

    @PutMapping("/{driverId}/selected-cars/{carId}")
    public DriverCarDTO selectCarForDriver(@PathVariable(name = "driverId") Long driverId,
                                           @PathVariable(name = "carId") Long carId) throws EntityNotFoundException, CarAlreadyInUseException, ConstraintsViolationException {
        return DriverCarMapper.makeDriverCarDTO(carDriverService.selectCarForDriver(driverId, carId));
    }

    @PutMapping("/{driverId}/deselected-cars/{carId}")
    public DriverCarDTO deselectCarForDriver(@PathVariable(name = "driverId") Long driverId,
                                             @PathVariable(name = "carId") Long carId) throws EntityNotFoundException, CarAlreadyInUseException, ConstraintsViolationException {
        return DriverCarMapper.makeDriverCarDTO(carDriverService.deselectCarForDriver(driverId, carId));
    }

    @GetMapping("/driver-or-car-attributes")
    public Page<DriverDTO> getDrivers(@RequestParam Map<String, Object> allRequestParams,
                                      @PageableDefault Pageable pageable)
    {
        return DriverMapper.makeDriverDTOPage(driverService.getDrivers(allRequestParams, pageable));
    }
}
