package com.car_rental.dao.daointerface;

import com.car_rental.entity.CarStatuses;
import java.util.List;

public interface CarStatusDAO {
    List<CarStatuses> findAllStatuses();
}
