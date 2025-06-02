package com.example.nosqlapi.main_services;

import com.example.nosqlapi.main_entity.Shift;
import com.example.nosqlapi.main_entity.ShiftKey;
import com.example.nosqlapi.main_repositories.ShiftRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    public Shift updateShift(ShiftKey key, Shift shift) {
        if (!shiftRepository.existsById(key)) {
            throw new RuntimeException("Shift not found");
        }
        shift.setKey(key);
        return shiftRepository.save(shift);
    }

    public void deleteShift(ShiftKey key) {
        shiftRepository.deleteById(key);
    }

    public Optional<Shift> getShift(ShiftKey key) {
        return shiftRepository.findById(key);
    }

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }
}
