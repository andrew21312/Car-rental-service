package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.CarIssueReportDAO;
import com.car_rental.dao.mapper.CarIssueReportRowMapper;
import com.car_rental.entity.PageResult;
import com.car_rental.form.car.CarIssueReport;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MySqlCarIssueReportDAO extends BasePaginationDAO implements CarIssueReportDAO {

    private static final String SELECT_ALL_REPORTS_QUERY = """
            SELECT
                car_issue_reports.id as report_id,
                car_issue_reports.car_id as report_car_id,
                car_issue_reports.created_at as report_created_at,
                car_issue_reports.message as report_message,
                car_issue_reports.status as report_status,
                car_issue_reports.resolved_at as report_resolved_at,
            
                cars.id as car_id,
                cars.plate_number as car_plate_number,
                cars.model_id as car_model_id,
            
                car_models.id as car_model_id,
                car_models.brand as car_model_brand,
                car_models.name as car_model_name,
                car_models.year as car_model_year
            
            FROM car_issue_reports
            JOIN cars ON cars.id = car_issue_reports.car_id
            JOIN car_models ON car_models.id = cars.model_id
            """;

    private static final String COUNT_REPORTS_QUERY =
            "SELECT COUNT(*) FROM car_issue_reports";

    private static final String INSERT_REPORT_QUERY =
            "INSERT INTO car_issue_reports (car_id, message) VALUES (?, ?)";

    private static final String UPDATE_REPORT_STATUS_QUERY =
            "UPDATE car_issue_reports SET status = 'RESOLVED', resolved_at = NOW() WHERE id = ?";

    private static final String UPDATE_RENTAL_STATUS_QUERY = """
            UPDATE rentals
            SET status_id = ?,
                payment_status = 'CANCELLED'
            WHERE car_id = ?
              AND (status_id = ? OR status_id = ?)
              AND start_date >= CURRENT_DATE
            """;

    private static final String UPDATE_CAR_STATUS_QUERY =
            "UPDATE cars SET status_id = ? WHERE id = ?";

    private static final String SELECT_CAR_BY_REPORT_QUERY =
            "SELECT car_id FROM car_issue_reports WHERE id = ?";

    private static final String DEFAULT_ORDER_BY = "car_issue_reports.created_at DESC";

    private final CarIssueReportRowMapper rowMapper;

    @Autowired
    public MySqlCarIssueReportDAO(JdbcTemplate jdbcTemplate,
                                  CarIssueReportRowMapper rowMapper) {
        super(jdbcTemplate);
        this.rowMapper = rowMapper;
    }

    @Override
    @Transactional
    public void createReport(CarIssueReport report, int rejectedStatusId, int approvedStatusId,
                             int readyForPickupStatusId, int maintenanceStatusId) {
        int carId = report.getCar().getId();

        jdbcTemplate.update(INSERT_REPORT_QUERY, carId, report.getMessage());
        jdbcTemplate.update(UPDATE_RENTAL_STATUS_QUERY, rejectedStatusId, carId,
                            readyForPickupStatusId, approvedStatusId);
        jdbcTemplate.update(UPDATE_CAR_STATUS_QUERY, maintenanceStatusId, carId);
    }

    @Override
    public PageResult<CarIssueReport> getCarsIssuesPage(String reportStatus, int page, int size) {
        QueryParams queryParams = buildReportFilterQuery(reportStatus);

        return executePaginationQuery(
                SELECT_ALL_REPORTS_QUERY,
                COUNT_REPORTS_QUERY,
                rowMapper,
                queryParams,
                page,
                size,
                DEFAULT_ORDER_BY
                                     );
    }

    @Override
    public void markResolved(int reportId, int availableStatusId) {
        jdbcTemplate.update(UPDATE_REPORT_STATUS_QUERY, reportId);

        Integer carId = getCarIdByReportId(reportId);
        if (carId == null) {
            throw new RuntimeException("Car not found for report id " + reportId);
        }

        jdbcTemplate.update(UPDATE_CAR_STATUS_QUERY, availableStatusId, carId);
    }

    private Integer getCarIdByReportId(int reportId) {
        return jdbcTemplate.queryForObject(SELECT_CAR_BY_REPORT_QUERY, Integer.class, reportId);
    }

    /**
     * Builds query conditions and parameters for filtering car issue reports by status.
     *
     * @param reportStatus the status to filter reports by
     * @return QueryParams object containing conditions and parameters
     */
    private QueryParams buildReportFilterQuery(String reportStatus) {
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (reportStatus != null && !reportStatus.trim().isEmpty()) {
            conditions.add("car_issue_reports.status = ?");
            parameters.add(reportStatus.trim());
        }

        return new QueryParams(conditions, parameters);
    }
}