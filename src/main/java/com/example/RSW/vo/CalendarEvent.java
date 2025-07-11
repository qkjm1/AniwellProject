package com.example.RSW.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class CalendarEvent {
    private int id;
    private int memberId;
    private Integer petId;
    private LocalDate eventDate;
    private String content;
    private LocalDateTime createdAt;
}
