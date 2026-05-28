package com.car_rental.convertor;

import com.car_rental.entity.CarModel;
import com.car_rental.service.CarModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToCarModelConverter implements Converter<String, CarModel> {

    private final CarModelService carModelService;

    @Autowired
    public StringToCarModelConverter(CarModelService carModelService) {
        this.carModelService = carModelService;
    }

    @Override
    public CarModel convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        int id = Integer.parseInt(source);
        return carModelService.getCarModelById(id);
    }
}
