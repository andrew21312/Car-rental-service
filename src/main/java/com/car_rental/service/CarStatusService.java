package com.car_rental.service;

import com.car_rental.entity.CarStatuses;
import java.util.List;

public interface CarStatusService {
    int getStatusId(String name);

    List<CarStatuses> getStatusList();

    CarStatuses getCarStatusById(int id);

    CarStatuses getCarStatusByName(String name);
}
