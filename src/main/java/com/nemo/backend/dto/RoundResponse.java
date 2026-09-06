package com.nemo.backend.dto;

public class RoundResponse {

    private int id;
    private String title;
    private String type;
    private int depthMeters;
    private String environment;
    private String instruction;

    public RoundResponse(
            int id,
            String title,
            String type,
            int depthMeters,
            String environment,
            String instruction
    ) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.depthMeters = depthMeters;
        this.environment = environment;
        this.instruction = instruction;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public int getDepthMeters() {
        return depthMeters;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getInstruction() {
        return instruction;
    }
}