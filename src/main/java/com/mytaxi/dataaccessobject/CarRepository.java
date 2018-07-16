package com.mytaxi.dataaccessobject;

import com.mytaxi.domainobject.CarDO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Database Access Object for driver table.
 */
public interface CarRepository extends CrudRepository<CarDO, Long> {

    List<CarDO> findAll();

    List<CarDO> findAllByDeletedIsFalse();

}
