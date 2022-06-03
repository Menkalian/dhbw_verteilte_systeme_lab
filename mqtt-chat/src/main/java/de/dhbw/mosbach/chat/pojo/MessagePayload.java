package de.dhbw.mosbach.chat.pojo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessagePayload {
    private String sender;
    private String text;
    private String clientId;
    private String topic;
}
