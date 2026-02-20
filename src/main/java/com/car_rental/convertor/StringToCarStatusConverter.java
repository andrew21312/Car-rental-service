package com.car_rental.convertor;

import com.car_rental.entity.CarStatuses;
import com.car_rental.service.CarStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToCarStatusConverter implements Converter<String, CarStatuses> {

    private final CarStatusService carStatusService;

    @Autowired
    public StringToCarStatusConverter(CarStatusService carStatusService) {
        this.carStatusService = carStatusService;
    }

    @Override
    public CarStatuses convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        int id = Integer.parseInt(source);
        return carStatusService.getCarStatusById(id);
    }
}
