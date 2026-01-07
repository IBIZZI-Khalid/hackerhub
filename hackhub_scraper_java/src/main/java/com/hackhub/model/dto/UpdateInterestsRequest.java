package com.hackhub.model.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateInterestsRequest {
    private Set<String> interests;
    private Set<String> eventTypes;
    private Set<String> providers;
}
