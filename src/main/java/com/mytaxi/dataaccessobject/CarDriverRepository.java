package com.mytaxi.dataaccessobject;

import com.mytaxi.domainobject.DriverCarDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarDriverRepository extends CrudRepository<DriverCarDO, Long>, JpaSpecificationExecutor<DriverCarDO> {

    List<DriverCarDO> findByCarDO_IdAndSelectedIsTrue(Long carDOId);

    DriverCarDO findByDriverDO_IdAndCarDO_Id(Long driverId, Long carId);

    Page<DriverCarDO> findAll(Pageable pageable);
}
