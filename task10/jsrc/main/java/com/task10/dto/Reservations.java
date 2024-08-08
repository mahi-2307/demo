package com.task10.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservations {
    private String tableNumber;
    private String clientNumber;
    private String phoneNumber;
    private String Date;
    private String slotTimeStart;
    private String slotTimeEnd;
}
