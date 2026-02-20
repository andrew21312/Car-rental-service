package com.car_rental.dao.daointerface;

import com.car_rental.entity.RentalStatuses;
import java.util.List;

public interface RentalStatusDAO {
    List<RentalStatuses> findAllStatuses();
}
