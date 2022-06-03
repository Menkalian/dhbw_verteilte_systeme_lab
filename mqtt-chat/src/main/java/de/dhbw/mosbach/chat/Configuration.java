package de.dhbw.mosbach.chat;

public enum Configuration {
    INSTANCE;

    public final String serverHost = "10.50.12.150";
    public final String clientId = "4b298fe1-67f3-43b7-9ab4-200488a83e90";
    public final String rootTopic = "/aichat/";
    public final String stateSubTopic = "clientstate";
    public final String chatSubTopic = "default";
}