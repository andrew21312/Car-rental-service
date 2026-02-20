package com.car_rental.dao.daointerface;

import com.car_rental.entity.PageResult;
import com.car_rental.form.car.CarIssueReport;

public interface CarIssueReportDAO {
    void createReport(CarIssueReport report, int rejectedStatusId, int approvedStatusId, int readyForPickupStatusId,
                      int maintenanceStatusId);

    PageResult<CarIssueReport> getCarsIssuesPage(String reportStatus, int page, int size);

    void markResolved(int reportId, int availableStatusId);
}
