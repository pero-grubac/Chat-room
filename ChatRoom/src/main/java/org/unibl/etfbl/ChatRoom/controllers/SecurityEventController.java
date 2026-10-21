package org.unibl.etfbl.ChatRoom.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.unibl.etfbl.ChatRoom.models.entities.SecurityEventEntity;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;

import java.util.List;

/** The SIEM's security log for administrators: the newest events first, optionally of one type. */
@RestController
@RequestMapping("/security-events")
public class SecurityEventController {

    @Autowired
    private SecurityEventService securityEventService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SecurityEventEntity> latest(@RequestParam(defaultValue = "100") int limit,
                                            @RequestParam(required = false) String type) {
        return securityEventService.latest(limit, type);
    }
}
