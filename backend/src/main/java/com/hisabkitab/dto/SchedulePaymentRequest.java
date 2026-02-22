package com.hisabkitab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePaymentRequest {
    @Size(max = 255, message = "Note must be at most 255 characters")
    private String note;
}
