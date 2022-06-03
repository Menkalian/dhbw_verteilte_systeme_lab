package de.dhbw.mosbach.lehre.kafkatankerspring.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TankerkoenigData {
    @NonNull
    private String date;
    @NonNull
    private String station;
    @NonNull
    private String postCode;
    @JsonAlias("pDiesel")
    private double pDiesel;
    @JsonAlias("pE5")
    private double pE5;
    @JsonAlias("pE10")
    private double pE10;
}
