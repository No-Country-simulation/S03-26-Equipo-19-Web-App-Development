package com.crm.app.filter;

import com.crm.app.model.enums.FunnelStatus;
import lombok.Data;

@Data
public class ContactFilters {

    private FunnelStatus funnelStatus;
    private Long ownerId;
    private String company;
    private String email;

    // getters/setters
}