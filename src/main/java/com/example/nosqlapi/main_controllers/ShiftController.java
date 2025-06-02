package com.example.nosqlapi.main_controllers;

import com.example.nosqlapi.main_entity.Shift;
import com.example.nosqlapi.main_entity.ShiftKey;
import com.example.nosqlapi.main_services.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    // Создать смену
    @PostMapping
    public ResponseEntity<Shift> createShift(@RequestBody Shift shift) {
        Shift created = shiftService.createShift(shift);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Обновить смену по составному ключу (employeeId и shiftDate)
    @PutMapping("/{employeeId}/{shiftDate}")
    public ResponseEntity<Shift> updateShift(
            @PathVariable UUID employeeId,
            @PathVariable String shiftDate,
            @RequestBody Shift shift) {

        LocalDate date = LocalDate.parse(shiftDate);
        ShiftKey key = new ShiftKey();
        key.setEmployee_id(employeeId);
        key.setShift_date(date);

        Shift updated = shiftService.updateShift(key, shift);
        return ResponseEntity.ok(updated);
    }

    // Удалить смену по ключу
    @DeleteMapping("/{employeeId}/{shiftDate}")
    public ResponseEntity<Void> deleteShift(
            @PathVariable UUID employeeId,
            @PathVariable String shiftDate) {

        LocalDate date = LocalDate.parse(shiftDate);
        ShiftKey key = new ShiftKey();
        key.setEmployee_id(employeeId);
        key.setShift_date(date);

        shiftService.deleteShift(key);
        return ResponseEntity.noContent().build();
    }

    // Получить смену по ключу
    @GetMapping("/{employeeId}/{shiftDate}")
    public ResponseEntity<Shift> getShift(
            @PathVariable UUID employeeId,
            @PathVariable String shiftDate) {

        LocalDate date = LocalDate.parse(shiftDate);
        ShiftKey key = new ShiftKey();
        key.setEmployee_id(employeeId);
        key.setShift_date(date);

        return shiftService.getShift(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Получить все смены
    @GetMapping
    public List<Shift> getAllShifts() {
        return shiftService.getAllShifts();
    }
}
