package com.car_rental.dao.daointerface;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.RentalExtra;
import java.util.List;

public interface RentalExtraDAO {
    List<RentalExtra> getAllExtras();

    PageResult<RentalExtra> getExtrasPage(String q, int page, int size);

    void addExtra(RentalExtra extra);

    void updateExtra(RentalExtra extraOld, RentalExtra extraNew);

    void deleteExtra(int id);
}
