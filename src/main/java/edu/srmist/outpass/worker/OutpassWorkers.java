package edu.srmist.outpass.worker;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class OutpassWorkers {

    private static final Logger log = LoggerFactory.getLogger(OutpassWorkers.class);

    @JobWorker(type = "check-outpass")
    public Map<String, Object> checkOutpass(
            @Variable String regNo,
            @Variable String outDate,
            @Variable String returnDate) {

        LocalDate out = LocalDate.parse(outDate);
        LocalDate ret = LocalDate.parse(returnDate);
        long nights = ChronoUnit.DAYS.between(out, ret);
        boolean valid = nights >= 0;
        boolean approved = valid && nights == 0;

        log.info("Out-pass check for {}: outDate={}, returnDate={}, nights={}, valid={}, autoApproved={}",
                regNo, outDate, returnDate, nights, valid, approved);

        return Map.of(
                "nights", nights,
                "valid", valid,
                "approved", approved
        );
    }

    @JobWorker(type = "issue-outpass")
    public Map<String, Object> issueOutpass(
            @Variable String regNo,
            @Variable String outDate,
            @Variable String studentName,
            @Variable String parentPhone,
            @Variable Boolean approved,
            @Variable String wardenRemarks) {

        boolean isApproved = Boolean.TRUE.equals(approved);
        String status = isApproved ? "ISSUED" : "REJECTED";
        String passNumber = isApproved ? "OP-" + regNo + "-" + outDate : null;
        String remarks = wardenRemarks == null || wardenRemarks.isBlank()
                ? "No remarks provided"
                : wardenRemarks;

        log.info("SMS to parent {}: Student {} ({}) out-pass decision = {}, remarks = {}",
                parentPhone, studentName, regNo, status, remarks);

        Map<String, Object> result = new HashMap<>();
        result.put("passStatus", status);
        result.put("passNumber", passNumber);
        return result;
    }
}
