package com.alexhqi.saveshare.core;

import java.util.UUID;

public class Save {

    private final UUID reference;
    private final String serviceId;

    public Save(UUID reference, String serviceId) {
        this.reference = reference;
        this.serviceId = serviceId;
    }

    public UUID getReference() {
        return reference;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String toString() {
        return reference.toString();
    }
}
