package com.car_rental.convertor;

import com.car_rental.entity.RentalStatuses;
import com.car_rental.service.RentalStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRentalStatusConverter implements Converter<String, RentalStatuses> {

    private final RentalStatusService rentalService;

    @Autowired
    public StringToRentalStatusConverter(RentalStatusService rentalService) {
        this.rentalService = rentalService;
    }

    @Override
    public RentalStatuses convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        int id = Integer.parseInt(source);
        return rentalService.getRentalStatusById(id);
    }
}
