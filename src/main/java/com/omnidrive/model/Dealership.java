package com.omnidrive.model;

import com.omnidrive.exception.DuplicateVehicleException;
import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contenedor central del modelo en OmniDrive (Patrón MVC).
 * Centraliza la gestión del inventario de vehículos, validaciones de negocio
 * y filtros de búsqueda sin intermediarios de repositorio o servicio.
 */
public class Dealership implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private final Map<String, Vehicle> vehicles;

    public Dealership(String name) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "OmniDrive Concesionario";
        } else {
            this.name = name.trim();
        }
        this.vehicles = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    /**
     * Registra un nuevo vehículo en el inventario validando placa única,
     * estado inicial de disponibilidad y revisión técnica aprobada para usados.
     *
     * @param vehicle Vehículo a registrar.
     * @throws InvalidVehicleDataException Si el vehículo es nulo o su estado inicial es inválido.
     * @throws DuplicateVehicleException Si ya existe un vehículo con la misma placa.
     * @throws TechnicalReviewRejectedException Si es usado y no cuenta con revisión técnica aprobada.
     */
    public void registerVehicle(Vehicle vehicle) throws InvalidVehicleDataException, DuplicateVehicleException, TechnicalReviewRejectedException {
        if (vehicle == null) {
            throw new InvalidVehicleDataException("El vehículo a registrar no puede ser nulo.");
        }

        String plate = vehicle.getPlate();
        if (vehicles.containsKey(plate)) {
            throw new DuplicateVehicleException(plate);
        }

        // RF-16: Estado inicial debe ser venta o alquiler
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE_FOR_SALE && vehicle.getStatus() != VehicleStatus.AVAILABLE_FOR_RENT) {
            throw new InvalidVehicleDataException("El estado inicial de disponibilidad debe ser 'Disponible para venta' o 'Disponible para alquiler'.");
        }

        // RF-14, RF-18: Revisión técnica aprobada obligatoria para usados
        if (vehicle.getCondition() == VehicleCondition.USED && !vehicle.isTechnicalReviewApproved()) {
            throw new TechnicalReviewRejectedException(plate);
        }

        vehicles.put(plate, vehicle);
    }

    /**
     * Busca un vehículo por su placa (insensible a mayúsculas/minúsculas).
     *
     * @param plate Placa a consultar.
     * @return El vehículo asociado o {@code null} si no existe.
     */
    public Vehicle findVehicleByPlate(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            return null;
        }
        return vehicles.get(plate.trim().toUpperCase());
    }

    /**
     * Verifica si una placa ya se encuentra registrada en el inventario.
     *
     * @param plate Placa a verificar.
     * @return {@code true} si la placa existe; {@code false} en caso contrario.
     */
    public boolean containsVehicle(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            return false;
        }
        return vehicles.containsKey(plate.trim().toUpperCase());
    }

    /**
     * Remueve un vehículo del inventario por su placa.
     *
     * @param plate Placa del vehículo a eliminar.
     * @return {@code true} si el vehículo fue eliminado; {@code false} si no existía.
     */
    public boolean removeVehicle(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            return false;
        }
        return vehicles.remove(plate.trim().toUpperCase()) != null;
    }

    /**
     * Obtiene una lista inmutable con todos los vehículos registrados.
     *
     * @return Lista no modificable de vehículos.
     */
    public List<Vehicle> getAllVehicles() {
        return Collections.unmodifiableList(List.copyOf(vehicles.values()));
    }

    /**
     * Filtra el inventario de vehículos según los criterios definidos.
     *
     * @param filter Criterios de filtrado. Si es {@code null}, retorna todos los vehículos.
     * @return Lista inmutable de vehículos que cumplen con los criterios.
     */
    public List<Vehicle> filterVehicles(VehicleFilter filter) {
        if (filter == null) {
            return getAllVehicles();
        }
        return vehicles.values().stream()
                .filter(filter::matches)
                .toList();
    }

    /**
     * Retorna la cantidad total de vehículos en el inventario.
     *
     * @return Total de vehículos.
     */
    public int getVehicleCount() {
        return vehicles.size();
    }
}
