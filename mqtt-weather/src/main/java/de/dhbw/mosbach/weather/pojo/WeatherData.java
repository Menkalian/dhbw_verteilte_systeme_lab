package de.dhbw.mosbach.weather.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class WeatherData {
    private double tempCurrent;
    private double tempMax;
    private double tempMin;
    private String comment;
    private Date timeStamp;
}
