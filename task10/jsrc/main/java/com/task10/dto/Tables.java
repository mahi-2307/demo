package com.task10.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tables {
    private int id;
    private NumberWrapper number; // Adjusted to match JSON structure
    private int places;
    private boolean isVip;
}


