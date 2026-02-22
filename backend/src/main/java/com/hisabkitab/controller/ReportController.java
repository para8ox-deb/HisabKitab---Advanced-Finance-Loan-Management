package com.hisabkitab.controller;

import com.hisabkitab.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ReportController — REST API for downloading CSV reports.
 *
 * These endpoints return a file download instead of JSON.
 * The key is setting the right HTTP headers:
 *   Content-Type: text/csv                   → Tells browser it's a CSV file
 *   Content-Disposition: attachment; filename → Triggers file download
 *
 * Endpoints:
 *   GET /api/reports/summary        → Download financial summary as CSV
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/reports/loans
     *
     * Download all loans for the current lender as a CSV file.
     * Browser will prompt "Save As" dialog.
     */
    @GetMapping("/loans")
    public ResponseEntity<byte[]> downloadLoanReport() {
        String csv = reportService.generateLoanReportCSV();
        return buildCSVResponse(csv, "loans_report.csv");
    }

    /**
     * GET /api/reports/summary
     *
     * Download a financial summary report as CSV.
     * Shows loan counts, total lent, interest earned, outstanding.
     */
    @GetMapping("/summary")
    public ResponseEntity<byte[]> downloadSummaryReport() {
        String csv = reportService.generateSummaryReportCSV();
        return buildCSVResponse(csv, "lending_summary_report.csv");
    }

    /**
     * Helper method to build a CSV file download response.
     *
     * Key HTTP headers for file download:
     *   Content-Type: text/csv → MIME type for CSV files
     *   Content-Disposition: attachment; filename="..." → Triggers download
     *
     * We return byte[] instead of String because:
     *   1. String in response body → Spring wraps it in JSON quotes
     *   2. byte[] → Spring sends raw bytes → proper CSV file download
     */
    private ResponseEntity<byte[]> buildCSVResponse(String csvContent, String filename) {
        byte[] bytes = csvContent.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}
