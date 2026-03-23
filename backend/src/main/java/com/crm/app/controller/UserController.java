package com.crm.app.controller;

import com.crm.app.dto.UserDTOs;
import com.crm.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salespersons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<List<UserDTOs.UserResponse>> list() {
        return ResponseEntity.ok(service.listSalespersons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTOs.UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSalesperson(id));
    }

    @PostMapping
    public ResponseEntity<UserDTOs.UserResponse> create(@Valid @RequestBody UserDTOs.CreateSalespersonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSalesperson(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTOs.UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDTOs.UpdateSalespersonRequest request
    ) {
        return ResponseEntity.ok(service.updateSalesperson(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivateSalesperson(id);
        return ResponseEntity.noContent().build();
    }
}