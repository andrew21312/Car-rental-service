package com.car_rental.service;

import com.car_rental.dao.daointerface.CarStatusDAO;
import com.car_rental.entity.CarStatuses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarStatusServiceImpl implements CarStatusService {
    private final Map<String, CarStatuses> statusNameToStatusMap;
    private final List<CarStatuses> statuses;

    @Autowired
    public CarStatusServiceImpl(CarStatusDAO statusDao) {
        this.statusNameToStatusMap = new HashMap<>();
        this.statuses = statusDao.findAllStatuses();
        loadStatuses();
    }

    private void loadStatuses() {
        for (CarStatuses status : statuses) {
            statusNameToStatusMap.put(status.getCarStatusName(), status);
        }
    }

    @Override
    public int getStatusId(String statusName) {
        CarStatuses status = statusNameToStatusMap.get(statusName);
        if (status == null) {
            throw new IllegalArgumentException("Unknown status: " + statusName);
        }
        return status.getId();
    }

    @Override
    public List<CarStatuses> getStatusList() {
        return statuses;
    }

    @Override
    public CarStatuses getCarStatusById(int id) {
        return statuses.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public CarStatuses getCarStatusByName(String statusName) {
        return statusNameToStatusMap.get(statusName);
    }
}
