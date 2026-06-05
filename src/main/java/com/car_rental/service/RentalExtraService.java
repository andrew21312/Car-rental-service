package com.car_rental.service;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.RentalExtra;
import java.util.List;

public interface RentalExtraService {
    int getExtraId(String name);

    List<RentalExtra> getExtrasList();

    RentalExtra getExtraById(int id);

    RentalExtra getExtraByName(String name);

    PageResult<RentalExtra> getExtrasPage(String q, int page, int size);

    void addExtra(RentalExtra extra);

    void updateExtra(RentalExtra extra);

    void deleteExtra(int id);

}
