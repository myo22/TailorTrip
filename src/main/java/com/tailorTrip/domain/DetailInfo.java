package com.tailorTrip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DetailInfo {

    @Column(name = "info_key")
    private String key; // Map의 key
    private String value; // Map의 value

}
