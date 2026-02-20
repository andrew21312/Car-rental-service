package com.car_rental.dao.mapper;

import com.car_rental.entity.Car;
import com.car_rental.entity.CarModel;
import com.car_rental.form.car.CarIssueReport;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class CarIssueReportRowMapper implements RowMapper<CarIssueReport> {

    @Override
    public CarIssueReport mapRow(ResultSet rs, int rowNum) throws SQLException {
        CarIssueReport report = new CarIssueReport();
        CarModel carModel = new CarModel();
        carModel.setId(rs.getInt("car_model_id"));
        carModel.setBrand(rs.getString("car_model_brand"));
        carModel.setModel(rs.getString("car_model_name"));
        carModel.setYear(rs.getInt("car_model_year"));

        Car car = new Car();
        car.setId(rs.getInt("car_id"));
        car.setPlateNumber(rs.getString("car_plate_number"));
        car.setModel(carModel);

        report.setId(rs.getInt("report_id"));
        report.setCar(car);
        report.setMessage(rs.getString("report_message"));
        report.setCreatedAt(rs.getTimestamp("report_created_at"));
        report.setStatus(CarIssueReport.ReportStatus.valueOf(rs.getString("report_status")));
        report.setResolvedAt(rs.getTimestamp("report_resolved_at"));
        return report;
    }
}
