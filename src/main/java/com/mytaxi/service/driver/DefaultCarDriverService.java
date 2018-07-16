package com.mytaxi.service.driver;

import com.mytaxi.dataaccessobject.CarDriverRepository;
import com.mytaxi.dataaccessobject.DriverRepository;
import com.mytaxi.domainobject.CarDO;
import com.mytaxi.domainobject.DriverCarDO;
import com.mytaxi.domainobject.DriverDO;
import com.mytaxi.domainvalue.OnlineStatus;
import com.mytaxi.exception.CarAlreadyInUseException;
import com.mytaxi.exception.ConstraintsViolationException;
import com.mytaxi.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DefaultCarDriverService implements CarDriverService {

    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(DefaultDriverService.class);

    private final CarDriverRepository carDriverRepository;

    private final CarService carService;

    private final DriverRepository driverRepository;

    public DefaultCarDriverService(final CarDriverRepository carDriverRepository, final CarService carService, final DriverRepository driverRepository) {
        this.carDriverRepository = carDriverRepository;
        this.carService = carService;
        this.driverRepository = driverRepository;
    }

    /**
     * Select car for a specific driver
     *
     * @param driverId
     * @param carId
     * @return
     * @throws EntityNotFoundException
     * @throws CarAlreadyInUseException
     */
    @Override
    public DriverCarDO selectCarForDriver(Long driverId, Long carId) throws EntityNotFoundException, CarAlreadyInUseException {

        LOG.debug("Selecting car: " + carId + " for driver: " + driverId);

        DriverCarDO driverCarDO = getDriverCarSelectedTrue(driverId, carId);

        if (driverCarDO == null || driverCarDO.getDriverDO().getId().equals(driverId)) {
            return insertOrUpdateDriverDOAsSelected(driverId, carId);
        }

        throw new CarAlreadyInUseException(carId);
    }

    /**
     * Deselect car for a specific driver
     *
     * @param driverId
     * @param carId
     * @return
     * @throws EntityNotFoundException
     */
    @Override
    public DriverCarDO deselectCarForDriver(Long driverId, Long carId) throws EntityNotFoundException, ConstraintsViolationException {

        DriverCarDO driverCarDO = getDriverCarSelectedTrue(driverId, carId);

        if (driverCarDO == null || !driverCarDO.getDriverDO().getId().equals(driverId)) {
            throw new EntityNotFoundException("Car is not yet selected for this driver.");
        }

        driverCarDO.setSelected(false);

        return create(driverCarDO);
    }

    /**
     * Find DriverCarDO
     *
     * @param driverId
     * @param carId
     * @return
     */
    @Override
    public DriverCarDO find(Long driverId, Long carId) {
        return carDriverRepository.findByDriverDO_IdAndCarDO_Id(driverId, carId);
    }

    @Override
    public Page<DriverCarDO> findCarDrivers(Map<String, Object> allRequestParams, Pageable pageable) {

        return carDriverRepository.findAll(buildCarDriverDOSearchSpecs(allRequestParams), pageable);
    }

    private Specification<DriverCarDO> buildCarDriverDOSearchSpecs(Map<String, Object> queryParams) {
        return new Specification<DriverCarDO>() {
            @Override
            public Predicate toPredicate(Root<DriverCarDO> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if (queryParams.containsKey("username")) {
                    String username = (String) queryParams.get("username");
                    predicates.add(criteriaBuilder.like(root.join("driverDO").get("username"), "%" + username + "%"));
                }

                if (queryParams.containsKey("onlineStatus")) {
                    String onlineStatus = (String) queryParams.get("onlineStatus");
                    //if (OnlineStatus.ONLINE.name())
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.join("driverDO").get("onlineStatus"), OnlineStatus.valueOf(onlineStatus.toUpperCase()))));
                }

                if (queryParams.containsKey("licensePlate")) {
                    String licensePlate = (String) queryParams.get("licensePlate");
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.join("carDO").get("licensePlate"), "%" + licensePlate + "%")));
                }

                if (queryParams.containsKey("convertible")) {
                    Boolean convertible = (Boolean) queryParams.get("convertible");
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.join("carDO").get("convertible"), convertible)));
                }

                if (queryParams.containsKey("rating")) {
                    Float rating = (Float) queryParams.get("rating");
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.join("carDO").get("rating"), rating)));
                }

                if (queryParams.containsKey("engineType")) {
                    String engineType = (String) queryParams.get("engineType");
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.join("carDO").get("engineType"), engineType)));
                }

                if (queryParams.containsKey("manufacturer")) {
                    String manufacturer = (String) queryParams.get("manufacturer");
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.join("carDO").get("manufacturer"), "%" + manufacturer + "%")));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
            }
        };
    }

    private void checkIfDriverExists(Long driverId) throws EntityNotFoundException {
        findDriverById(driverId);
    }

    private DriverCarDO getDriverCarSelectedTrue(Long driverId, Long carId) throws EntityNotFoundException {
        checkIfDriverExists(driverId);

        CarDO carDO = carService.find(carId);

        List<DriverCarDO> driverCarDOList = carDriverRepository.findByCarDO_IdAndSelectedIsTrue(carDO.getId());
        if (driverCarDOList != null && driverCarDOList.size() > 1) {
            throw new IllegalStateException("A car is selected by an offline driver.");
        }

        return driverCarDOList == null || driverCarDOList.isEmpty() ? null : driverCarDOList.get(0);
    }

    private DriverCarDO insertOrUpdateDriverDOAsSelected(Long driverId, Long carId) throws EntityNotFoundException {
        DriverCarDO driverCarDO = find(driverId, carId);

        if (driverCarDO == null) {
            log.debug("Car selected for the first time");
            driverCarDO = new DriverCarDO();
            driverCarDO.setDriverDO(findDriverById(driverId));
            driverCarDO.setCarDO(carService.find(carId));
        }

        driverCarDO.setSelected(true);

        return carDriverRepository.save(driverCarDO);
    }

    private DriverCarDO create(DriverCarDO driverCarDO) throws ConstraintsViolationException {
        try {
            return carDriverRepository.save(driverCarDO);
        } catch (DataIntegrityViolationException e)
        {
            LOG.warn("Some constraints are thrown due to driver car selection");
            throw new ConstraintsViolationException(e.getMessage());
        }
    }

    private DriverDO findDriverById(Long id) throws EntityNotFoundException {
        return driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Could not find entity with id: " + id));
    }
}
