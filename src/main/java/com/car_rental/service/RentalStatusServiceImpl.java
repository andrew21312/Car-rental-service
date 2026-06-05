package com.car_rental.service;

import com.car_rental.dao.daointerface.RentalStatusDAO;
import com.car_rental.entity.RentalStatuses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RentalStatusServiceImpl implements RentalStatusService {
    private final Map<String, RentalStatuses> statusMap;
    private final List<RentalStatuses> statusList;

    @Autowired
    public RentalStatusServiceImpl(RentalStatusDAO statusDao) {
        this.statusMap = new HashMap<>();
        this.statusList = statusDao.findAllStatuses();
        loadStatuses(statusList);
    }

    private void loadStatuses(List<RentalStatuses> statusList) {
        for (RentalStatuses status : statusList) {
            statusMap.put(status.getName(), status);
        }
    }

    @Override
    public int getStatusId(String name) {
        RentalStatuses status = statusMap.get(name);
        if (status == null) throw new IllegalArgumentException("Unknown status: " + name);
        return status.getId();
    }

    @Override
    public List<RentalStatuses> getStatusList() {
        return statusList;
    }

    @Override
    public RentalStatuses getRentalStatusById(int id) {
        return statusList.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public RentalStatuses getRentalStatusByName(String name) {
        return statusMap.get(name);
    }
}
