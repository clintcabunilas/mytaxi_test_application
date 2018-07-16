package com.mytaxi.exception;

public class CarAlreadyInUseException extends Exception {

    public CarAlreadyInUseException(Long carId) {
        super("Sorry but the car with id: " + carId + " is already selected by another online driver. Please select another car instead.");
    }
}
