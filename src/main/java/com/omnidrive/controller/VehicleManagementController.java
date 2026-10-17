package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import com.omnidrive.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Controlador de la vista del Inventario de Vehículos en OmniDrive.
 * Proporciona tabla interactiva, filtros en tiempo real y panel lateral con ficha técnica completa y foto.
 */
public class VehicleManagementController {

    @FXML
    private TableView<Vehicle> vehicleTable;

    @FXML
    private TableColumn<Vehicle, String> colPlate;

    @FXML
    private TableColumn<Vehicle, String> colBrand;

    @FXML
    private TableColumn<Vehicle, String> colType;

    @FXML
    private TableColumn<Vehicle, String> colModelYear;

    @FXML
    private TableColumn<Vehicle, String> colFuelType;

    @FXML
    private TableColumn<Vehicle, String> colCondition;

    @FXML
    private TableColumn<Vehicle, String> colStatus;

    @FXML
    private TableColumn<Vehicle, String> colBasePrice;

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cmbConditionFilter;

    @FXML
    private ComboBox<String> cmbStatusFilter;

    @FXML
    private ImageView imgVehiclePhoto;

    @FXML
    private Label lblDetailPlate;

    @FXML
    private Label lblDetailBrand;

    @FXML
    private Label lblDetailType;

    @FXML
    private Label lblDetailStatus;

    @FXML
    private Label lblDetailPrice;

    @FXML
    private Label lblDetailTechnicalSpecs;

    @FXML
    private VBox detailPanel;

    @FXML
    private Button btnNewVehicle;

    private final ObservableList<Vehicle> masterVehicleList = FXCollections.observableArrayList();
    private Vehicle selectedVehicle;

    @FXML
    public void initialize() {
        setupColumns();
        setupFilters();
        setupSelectionListener();
        setupSearchListener();
        loadVehicles();
    }

    private void setupColumns() {
        if (colPlate != null) {
            colPlate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPlate()));
        }
        if (colBrand != null) {
            colBrand.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBrand()));
        }
        if (colType != null) {
            colType.setCellValueFactory(cell -> new SimpleStringProperty(getVehicleTypeName(cell.getValue())));
        }
        if (colModelYear != null) {
            colModelYear.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getModelYear())));
        }
        if (colFuelType != null) {
            colFuelType.setCellValueFactory(cell -> new SimpleStringProperty(formatFuelType(cell.getValue())));
        }
        if (colCondition != null) {
            colCondition.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getCondition() == VehicleCondition.NEW ? "Nuevo" : "Usado"
            ));
        }
        if (colStatus != null) {
            colStatus.setCellValueFactory(cell -> new SimpleStringProperty(formatStatus(cell.getValue().getStatus())));
        }
        if (colBasePrice != null) {
            colBasePrice.setCellValueFactory(cell -> new SimpleStringProperty(formatPrice(cell.getValue().getBasePrice())));
        }
    }

    private void setupFilters() {
        if (cmbConditionFilter != null) {
            cmbConditionFilter.setItems(FXCollections.observableArrayList(
                    "Todas las Condiciones", "Nuevo", "Usado"
            ));
            cmbConditionFilter.setValue("Todas las Condiciones");
            cmbConditionFilter.setOnAction(e -> applyFilters());
        }

        if (cmbStatusFilter != null) {
            cmbStatusFilter.setItems(FXCollections.observableArrayList(
                    "Todos los Estados", "Disponible para Venta", "Disponible para Alquiler", "Alquilado", "Vendido"
            ));
            cmbStatusFilter.setValue("Todos los Estados");
            cmbStatusFilter.setOnAction(e -> applyFilters());
        }
    }

    private void setupSelectionListener() {
        if (vehicleTable != null) {
            vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    selectVehicle(newV);
                }
            });
        }
    }

    private void setupSearchListener() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        }
    }

    public void loadVehicles() {
        Dealership dealership = AppContext.getDealership();
        if (dealership == null) {
            return;
        }

        masterVehicleList.clear();
        masterVehicleList.addAll(dealership.getAllVehicles());
        applyFilters();

        if (!masterVehicleList.isEmpty()) {
            selectVehicle(masterVehicleList.get(0));
            if (vehicleTable != null) {
                vehicleTable.getSelectionModel().select(0);
            }
        }
    }

    public void applyFilters() {
        if (vehicleTable == null) {
            return;
        }

        String search = txtSearch != null ? txtSearch.getText() : null;
        String query = (search != null) ? search.trim().toLowerCase() : "";

        String conditionFilter = cmbConditionFilter != null ? cmbConditionFilter.getValue() : "Todas las Condiciones";
        String statusFilter = cmbStatusFilter != null ? cmbStatusFilter.getValue() : "Todos los Estados";

        List<Vehicle> filtered = masterVehicleList.stream()
                .filter(v -> matchesSearch(v, query))
                .filter(v -> matchesCondition(v, conditionFilter))
                .filter(v -> matchesStatus(v, statusFilter))
                .toList();

        vehicleTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private boolean matchesSearch(Vehicle v, String query) {
        if (query.isEmpty()) return true;
        return v.getPlate().toLowerCase().contains(query)
                || v.getBrand().toLowerCase().contains(query)
                || getVehicleTypeName(v).toLowerCase().contains(query);
    }

    private boolean matchesCondition(Vehicle v, String conditionFilter) {
        if (conditionFilter == null || conditionFilter.equals("Todas las Condiciones")) return true;
        if (conditionFilter.equals("Nuevo")) return v.getCondition() == VehicleCondition.NEW;
        if (conditionFilter.equals("Usado")) return v.getCondition() == VehicleCondition.USED;
        return true;
    }

    private boolean matchesStatus(Vehicle v, String statusFilter) {
        if (statusFilter == null || statusFilter.equals("Todos los Estados")) return true;
        if (statusFilter.equals("Disponible para Venta")) return v.getStatus() == VehicleStatus.AVAILABLE_FOR_SALE;
        if (statusFilter.equals("Disponible para Alquiler")) return v.getStatus() == VehicleStatus.AVAILABLE_FOR_RENT;
        if (statusFilter.equals("Alquilado")) return v.getStatus() == VehicleStatus.RENTED;
        if (statusFilter.equals("Vendido")) return v.getStatus() == VehicleStatus.SOLD;
        return true;
    }

    public void selectVehicle(Vehicle vehicle) {
        this.selectedVehicle = vehicle;
        if (vehicle == null) {
            return;
        }

        if (lblDetailPlate != null) {
            lblDetailPlate.setText(vehicle.getPlate());
        }
        if (lblDetailBrand != null) {
            lblDetailBrand.setText(vehicle.getBrand());
        }
        if (lblDetailType != null) {
            lblDetailType.setText(getVehicleTypeName(vehicle));
        }
        if (lblDetailStatus != null) {
            lblDetailStatus.setText(formatStatus(vehicle.getStatus()));
        }
        if (lblDetailPrice != null) {
            lblDetailPrice.setText(formatPrice(vehicle.getBasePrice()));
        }
        if (lblDetailTechnicalSpecs != null) {
            lblDetailTechnicalSpecs.setText(buildTechnicalSpecs(vehicle));
        }

        updateVehiclePhoto(vehicle.getPhotoUrl());
    }

    private void updateVehiclePhoto(String photoUrl) {
        if (imgVehiclePhoto == null) {
            return;
        }

        Image loadedImage = null;
        if (photoUrl != null && !photoUrl.isBlank()) {
            File file = new File(photoUrl);
            if (file.exists()) {
                try {
                    loadedImage = new Image(file.toURI().toString());
                } catch (Exception ignored) {
                }
            }
        }

        if (loadedImage == null || loadedImage.isError()) {
            InputStream defaultStream = getClass().getResourceAsStream("/com/omnidrive/view/images/default_vehicle.png");
            if (defaultStream != null) {
                loadedImage = new Image(defaultStream);
            }
        }

        if (loadedImage != null) {
            imgVehiclePhoto.setImage(loadedImage);
        }
    }

    public String getVehicleTypeName(Vehicle v) {
        if (v instanceof Sedan) return "Sedán";
        if (v instanceof SportsCar) return "Deportivo";
        if (v instanceof Suv) return "Camioneta / SUV";
        if (v instanceof PickUp) return "Pick Up";
        if (v instanceof Van) return "Van";
        if (v instanceof Bus) return "Bus";
        if (v instanceof Truck) return "Camión";
        if (v instanceof Motorcycle) return "Moto";
        return "Vehículo";
    }

    private String formatStatus(VehicleStatus status) {
        if (status == null) return "Desconocido";
        return switch (status) {
            case AVAILABLE_FOR_SALE -> "Disponible para Venta";
            case AVAILABLE_FOR_RENT -> "Disponible para Alquiler";
            case RENTED -> "Alquilado";
            case SOLD -> "Vendido";
            case MAINTENANCE -> "En Mantenimiento";
        };
    }

    private String formatPrice(double price) {
        return String.format("$ %,.0f", price);
    }

    private String formatFuelType(Vehicle v) {
        if (v.getFuelType() == null) return "N/A";
        return switch (v.getFuelType()) {
            case GASOLINE -> "Gasolina";
            case DIESEL -> "Diésel";
            case ELECTRIC -> "Eléctrico";
            case HYBRID -> "Híbrido";
        };
    }

    private String buildTechnicalSpecs(Vehicle v) {
        StringBuilder sb = new StringBuilder();
        sb.append("• Modelo / Año: ").append(v.getModelYear()).append("\n");
        sb.append("• Transmisión: ").append(v.getTransmissionType() != null ? v.getTransmissionType().name() : "N/A").append("\n");
        sb.append("• Velocidad máxima: ").append(v.getMaxSpeedKmH()).append(" km/h\n");
        sb.append("• Cilindraje: ").append(v.getCylinderCapacityCc()).append(" cc\n");
        sb.append("• Cambios: ").append(v.getSpeedCount()).append(" velocidades\n");
        sb.append("• Condición: ").append(v.getCondition() == VehicleCondition.NEW ? "Nuevo" : "Usado").append("\n");
        sb.append("• Revisión técnica: ").append(v.isTechnicalReviewApproved() ? "Aprobada" : "No requerida / Pendiente").append("\n");

        if (v.getElectricDetails() != null) {
            sb.append("• Autonomía eléctrica: ").append(v.getElectricDetails().getAutonomyKm()).append(" km\n");
            sb.append("• Tiempo de recarga: ").append(v.getElectricDetails().getAverageChargingHours()).append(" h\n");
        }

        if (v.getHybridDetails() != null) {
            sb.append("• Híbrido enchufable: ").append(v.getHybridDetails().isPlugIn() ? "Sí" : "No").append("\n");
            sb.append("• Híbrido ligero: ").append(v.getHybridDetails().isMildHybrid() ? "Sí" : "No").append("\n");
        }

        // Especificaciones por jerarquía
        if (v instanceof PassengerVehicle pv) {
            sb.append("• Pasajeros: ").append(pv.getPassengerCount()).append("\n");
            sb.append("• Puertas: ").append(pv.getDoorsCount()).append("\n");
            sb.append("• Bolsas de aire: ").append(pv.getAirbagsCount()).append("\n");
            sb.append("• Aire acondicionado: ").append(pv.hasAirConditioning() ? "Sí" : "No").append("\n");
            sb.append("• Cámara de reversa: ").append(pv.hasReverseCamera() ? "Sí" : "No").append("\n");
            sb.append("• Frenos ABS: ").append(pv.hasAbs() ? "Sí" : "No").append("\n");
        }

        if (v instanceof Sedan s) {
            sb.append("• Maletero: ").append(s.getTrunkCapacityLiters()).append(" L\n");
            sb.append("• Velocidad de crucero: ").append(s.hasCruiseControl() ? "Sí" : "No").append("\n");
            sb.append("• Sensores de colisión: ").append(s.hasCollisionSensors() ? "Sí" : "No").append("\n");
            sb.append("• Asistente carril: ").append(s.hasLaneKeepingAssist() ? "Sí" : "No").append("\n");
        } else if (v instanceof Suv suv) {
            sb.append("• Maletero: ").append(suv.getTrunkCapacityLiters()).append(" L\n");
            sb.append("• Tracción 4x4: ").append(suv.isFourByFour() ? "Sí" : "No").append("\n");
            sb.append("• Asistente carril: ").append(suv.hasLaneKeepingAssist() ? "Sí" : "No").append("\n");
        } else if (v instanceof PickUp pu) {
            sb.append("• Tracción 4x4: ").append(pu.isFourByFour() ? "Sí" : "No").append("\n");
            sb.append("• Capacidad caja de carga: ").append(pu.getCargoBedCapacityKg()).append(" kg\n");
        } else if (v instanceof Van van) {
            sb.append("• Maletero: ").append(van.getTrunkCapacityLiters()).append(" L\n");
        } else if (v instanceof SportsCar sc) {
            sb.append("• Potencia: ").append(sc.getHorsepower()).append(" HP\n");
            sb.append("• 0 a 100 km/h: ").append(sc.getZeroToHundredSecs()).append(" s\n");
        } else if (v instanceof Bus bus) {
            sb.append("• Maletero: ").append(bus.getTrunkCapacityLiters()).append(" L\n");
            sb.append("• Número de ejes: ").append(bus.getAxleCount()).append("\n");
            sb.append("• Salidas de emergencia: ").append(bus.getEmergencyExitCount()).append("\n");
        } else if (v instanceof Truck trk) {
            sb.append("• Capacidad de carga: ").append(trk.getLoadCapacityTons()).append(" Ton\n");
            String truckTypeDesc = trk.getTruckType() != null ? switch (trk.getTruckType()) {
                case BOX_TRUCK -> "Furgón";
                case TANKER -> "Cisterna";
                case HOPPER -> "Tolva";
                case FLATBED -> "Plataforma";
            } : "N/A";
            sb.append("• Tipo: ").append(truckTypeDesc).append("\n");
            sb.append("• Frenos de aire: ").append(trk.hasAirBrakes() ? "Sí" : "No").append("\n");
            sb.append("• Frenos ABS: ").append(trk.hasAbs() ? "Sí" : "No").append("\n");
            sb.append("• Número de ejes: ").append(trk.getAxleCount()).append("\n");
        }

        return sb.toString();
    }

    @FXML
    private void handleNewVehicle() {
        try {
            URL resource = getClass().getResource("/com/omnidrive/view/VehicleFormDialog.fxml");
            if (resource == null) {
                AlertUtil.showInfo("Registro de Vehículos", "El diálogo de registro estará disponible con el formulario modal (T9).");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent dialogRoot = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("OmniDrive — Registrar Nuevo Vehículo");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            loadVehicles();
        } catch (Exception e) {
            AlertUtil.showError("Error al Abrir Formulario", e.getMessage());
        }
    }

    // Getters y Setters para inyección y pruebas unitarias

    public TableView<Vehicle> getVehicleTable() {
        return vehicleTable;
    }

    public void setVehicleTable(TableView<Vehicle> vehicleTable) {
        this.vehicleTable = vehicleTable;
    }

    public TableColumn<Vehicle, String> getColPlate() {
        return colPlate;
    }

    public void setColPlate(TableColumn<Vehicle, String> colPlate) {
        this.colPlate = colPlate;
    }

    public TableColumn<Vehicle, String> getColBrand() {
        return colBrand;
    }

    public void setColBrand(TableColumn<Vehicle, String> colBrand) {
        this.colBrand = colBrand;
    }

    public TableColumn<Vehicle, String> getColType() {
        return colType;
    }

    public void setColType(TableColumn<Vehicle, String> colType) {
        this.colType = colType;
    }

    public TableColumn<Vehicle, String> getColModelYear() {
        return colModelYear;
    }

    public void setColModelYear(TableColumn<Vehicle, String> colModelYear) {
        this.colModelYear = colModelYear;
    }

    public TableColumn<Vehicle, String> getColFuelType() {
        return colFuelType;
    }

    public void setColFuelType(TableColumn<Vehicle, String> colFuelType) {
        this.colFuelType = colFuelType;
    }

    public TableColumn<Vehicle, String> getColCondition() {
        return colCondition;
    }

    public void setColCondition(TableColumn<Vehicle, String> colCondition) {
        this.colCondition = colCondition;
    }

    public TableColumn<Vehicle, String> getColStatus() {
        return colStatus;
    }

    public void setColStatus(TableColumn<Vehicle, String> colStatus) {
        this.colStatus = colStatus;
    }

    public TableColumn<Vehicle, String> getColBasePrice() {
        return colBasePrice;
    }

    public void setColBasePrice(TableColumn<Vehicle, String> colBasePrice) {
        this.colBasePrice = colBasePrice;
    }

    public TextField getTxtSearch() {
        return txtSearch;
    }

    public void setTxtSearch(TextField txtSearch) {
        this.txtSearch = txtSearch;
    }

    public ComboBox<String> getCmbConditionFilter() {
        return cmbConditionFilter;
    }

    public void setCmbConditionFilter(ComboBox<String> cmbConditionFilter) {
        this.cmbConditionFilter = cmbConditionFilter;
    }

    public ComboBox<String> getCmbStatusFilter() {
        return cmbStatusFilter;
    }

    public void setCmbStatusFilter(ComboBox<String> cmbStatusFilter) {
        this.cmbStatusFilter = cmbStatusFilter;
    }

    public ImageView getImgVehiclePhoto() {
        return imgVehiclePhoto;
    }

    public void setImgVehiclePhoto(ImageView imgVehiclePhoto) {
        this.imgVehiclePhoto = imgVehiclePhoto;
    }

    public Label getLblDetailPlate() {
        return lblDetailPlate;
    }

    public void setLblDetailPlate(Label lblDetailPlate) {
        this.lblDetailPlate = lblDetailPlate;
    }

    public Label getLblDetailBrand() {
        return lblDetailBrand;
    }

    public void setLblDetailBrand(Label lblDetailBrand) {
        this.lblDetailBrand = lblDetailBrand;
    }

    public Label getLblDetailType() {
        return lblDetailType;
    }

    public void setLblDetailType(Label lblDetailType) {
        this.lblDetailType = lblDetailType;
    }

    public Label getLblDetailStatus() {
        return lblDetailStatus;
    }

    public void setLblDetailStatus(Label lblDetailStatus) {
        this.lblDetailStatus = lblDetailStatus;
    }

    public Label getLblDetailPrice() {
        return lblDetailPrice;
    }

    public void setLblDetailPrice(Label lblDetailPrice) {
        this.lblDetailPrice = lblDetailPrice;
    }

    public Label getLblDetailTechnicalSpecs() {
        return lblDetailTechnicalSpecs;
    }

    public void setLblDetailTechnicalSpecs(Label lblDetailTechnicalSpecs) {
        this.lblDetailTechnicalSpecs = lblDetailTechnicalSpecs;
    }

    public VBox getDetailPanel() {
        return detailPanel;
    }

    public void setDetailPanel(VBox detailPanel) {
        this.detailPanel = detailPanel;
    }

    public Button getBtnNewVehicle() {
        return btnNewVehicle;
    }

    public void setBtnNewVehicle(Button btnNewVehicle) {
        this.btnNewVehicle = btnNewVehicle;
    }
}
