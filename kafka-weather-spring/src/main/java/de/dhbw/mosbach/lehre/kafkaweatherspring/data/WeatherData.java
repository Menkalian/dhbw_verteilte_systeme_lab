package de.dhbw.mosbach.lehre.kafkaweatherspring.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherData {
    private double tempCurrent;
    private double tempMax;
    private double tempMin;
    private String comment;
    private String timeStamp;
    private String city;
    private long cityId;
}
