package com.car_rental.service;

import com.car_rental.entity.RentalStatuses;
import java.util.List;

public interface RentalStatusService {

    int getStatusId(String name);

    List<RentalStatuses> getStatusList();

    RentalStatuses getRentalStatusById(int id);

    RentalStatuses getRentalStatusByName(String name);
}
