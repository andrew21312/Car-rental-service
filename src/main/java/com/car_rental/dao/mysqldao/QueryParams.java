package com.car_rental.dao.mysqldao;

import java.util.List;

// Внутрішній клас для зберігання умов і параметрів запиту.
public record QueryParams(List<String> conditions, List<Object> params) {
}
