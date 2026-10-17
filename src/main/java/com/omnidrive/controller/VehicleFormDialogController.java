package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.exception.OmniDriveException;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.*;
import com.omnidrive.util.AlertUtil;
import com.omnidrive.validation.InputValidator;
import com.omnidrive.validation.ValidationResult;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador del formulario modal reactivo para el registro de los 8 tipos de vehículos en OmniDrive.
 * Muestra u oculta dinámicamente los campos pertinentes según el tipo de vehículo y tecnología de combustible.
 */
public class VehicleFormDialogController {

    @FXML
    private ComboBox<String> cmbVehicleType;

    @FXML
    private TextField txtPlate;

    @FXML
    private TextField txtBrand;

    @FXML
    private TextField txtModelYear;

    @FXML
    private TextField txtSpeedCount;

    @FXML
    private TextField txtMaxSpeed;

    @FXML
    private TextField txtCylinderCapacity;

    @FXML
    private ComboBox<TransmissionType> cmbTransmission;

    @FXML
    private ComboBox<FuelType> cmbFuelType;

    @FXML
    private ComboBox<VehicleCondition> cmbCondition;

    @FXML
    private ComboBox<VehicleStatus> cmbStatus;

    @FXML
    private TextField txtBasePrice;

    @FXML
    private TextField txtPhotoUrl;

    @FXML
    private CheckBox chkTechnicalReview;

    @FXML
    private ImageView imgPreview;

    @FXML
    private Button btnBrowsePhoto;

    // Paneles de Combustible
    @FXML
    private VBox paneElectric;

    @FXML
    private TextField txtAutonomyKm;

    @FXML
    private TextField txtChargingHours;

    @FXML
    private VBox paneHybrid;

    @FXML
    private CheckBox chkPlugIn;

    @FXML
    private CheckBox chkMildHybrid;

    // Paneles por Tipo de Vehículo
    @FXML
    private VBox panePassenger;

    @FXML
    private TextField txtPassengerCount;

    @FXML
    private TextField txtDoorsCount;

    @FXML
    private TextField txtAirbagsCount;

    @FXML
    private CheckBox chkAirConditioning;

    @FXML
    private CheckBox chkReverseCamera;

    @FXML
    private CheckBox chkAbs;

    @FXML
    private VBox paneTrunk;

    @FXML
    private TextField txtTrunkCapacity;

    @FXML
    private VBox paneSedanSuv;

    @FXML
    private CheckBox chkCruiseControl;

    @FXML
    private CheckBox chkCollisionSensors;

    @FXML
    private CheckBox chkCrossTraffic;

    @FXML
    private CheckBox chkLaneAssist;

    @FXML
    private VBox paneFourByFour;

    @FXML
    private CheckBox chkFourByFour;

    @FXML
    private VBox panePickUp;

    @FXML
    private TextField txtBedCapacity;

    @FXML
    private VBox paneSportsCar;

    @FXML
    private TextField txtHorsepower;

    @FXML
    private TextField txtZeroToHundred;

    @FXML
    private VBox paneBus;

    @FXML
    private TextField txtAxleCount;

    @FXML
    private TextField txtEmergencyExits;

    @FXML
    private VBox paneTruck;

    @FXML
    private TextField txtLoadCapacityTons;

    @FXML
    private CheckBox chkTruckAirBrakes;

    @FXML
    private CheckBox chkTruckAirConditioning;

    @FXML
    private CheckBox chkTruckAbs;

    @FXML
    private TextField txtTruckAxles;

    @FXML
    private ComboBox<TruckType> cmbTruckType;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private Stage dialogStage;
    private Consumer<Vehicle> onVehicleRegistered;

    @FXML
    public void initialize() {
        populateCombos();
        setupListeners();
        attachErrorClearingListeners();
        updateFormVisibility();
        updatePreviewPhoto(null);
    }

    private void populateCombos() {
        if (cmbVehicleType != null && cmbVehicleType.getItems().isEmpty()) {
            cmbVehicleType.setItems(FXCollections.observableArrayList(
                    "Sedán", "Deportivo", "Camioneta / SUV", "Pick Up", "Van", "Bus", "Camión", "Moto"
            ));
            cmbVehicleType.setValue("Sedán");
        }

        if (cmbTransmission != null && cmbTransmission.getItems().isEmpty()) {
            cmbTransmission.setItems(FXCollections.observableArrayList(TransmissionType.values()));
            cmbTransmission.setValue(TransmissionType.AUTOMATIC);
        }

        if (cmbFuelType != null && cmbFuelType.getItems().isEmpty()) {
            cmbFuelType.setItems(FXCollections.observableArrayList(FuelType.values()));
            cmbFuelType.setValue(FuelType.GASOLINE);
        }

        if (cmbCondition != null && cmbCondition.getItems().isEmpty()) {
            cmbCondition.setItems(FXCollections.observableArrayList(VehicleCondition.values()));
            cmbCondition.setValue(VehicleCondition.NEW);
        }

        if (cmbStatus != null && cmbStatus.getItems().isEmpty()) {
            cmbStatus.setItems(FXCollections.observableArrayList(
                    VehicleStatus.AVAILABLE_FOR_SALE, VehicleStatus.AVAILABLE_FOR_RENT
            ));
            cmbStatus.setValue(VehicleStatus.AVAILABLE_FOR_SALE);
        }

        if (cmbTruckType != null && cmbTruckType.getItems().isEmpty()) {
            cmbTruckType.setItems(FXCollections.observableArrayList(TruckType.values()));
            cmbTruckType.setValue(TruckType.BOX_TRUCK);
        }
    }

    private void setupListeners() {
        if (cmbVehicleType != null) {
            cmbVehicleType.setOnAction(e -> updateFormVisibility());
        }
        if (cmbFuelType != null) {
            cmbFuelType.setOnAction(e -> updateFormVisibility());
        }
        if (cmbCondition != null) {
            cmbCondition.setOnAction(e -> {
                if (cmbCondition.getValue() == VehicleCondition.USED && chkTechnicalReview != null) {
                    chkTechnicalReview.setStyle("-fx-text-fill: #1e40af; -fx-font-weight: bold;");
                }
            });
        }
    }

    public void updateFormVisibility() {
        // 1. Visibilidad por combustible
        FuelType fuel = cmbFuelType != null ? cmbFuelType.getValue() : FuelType.GASOLINE;
        boolean isElectric = fuel == FuelType.ELECTRIC;
        boolean isHybrid = fuel == FuelType.HYBRID;

        setNodeVisibleAndManaged(paneElectric, isElectric);
        setNodeVisibleAndManaged(paneHybrid, isHybrid);

        // 2. Visibilidad por tipo de vehículo
        String type = cmbVehicleType != null ? cmbVehicleType.getValue() : "Sedán";
        if (type == null) type = "Sedán";

        boolean isSedan = "Sedán".equals(type);
        boolean isSports = "Deportivo".equals(type);
        boolean isSuv = "Camioneta / SUV".equals(type);
        boolean isPickUp = "Pick Up".equals(type);
        boolean isVan = "Van".equals(type);
        boolean isBus = "Bus".equals(type);
        boolean isTruck = "Camión".equals(type);

        boolean hasPassengers = isSedan || isSports || isSuv || isPickUp || isVan || isBus;
        boolean hasTrunk = isSedan || isSuv || isVan || isBus;
        boolean isSedanOrSuv = isSedan || isSuv;
        boolean isFourByFourType = isSuv || isPickUp;

        setNodeVisibleAndManaged(panePassenger, hasPassengers);
        setNodeVisibleAndManaged(paneTrunk, hasTrunk);
        setNodeVisibleAndManaged(paneSedanSuv, isSedanOrSuv);
        setNodeVisibleAndManaged(paneFourByFour, isFourByFourType);
        setNodeVisibleAndManaged(panePickUp, isPickUp);
        setNodeVisibleAndManaged(paneSportsCar, isSports);
        setNodeVisibleAndManaged(paneBus, isBus);
        setNodeVisibleAndManaged(paneTruck, isTruck);
    }

    private void setNodeVisibleAndManaged(VBox node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    @FXML
    public void handleBrowsePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Fotografía del Vehículo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = dialogStage;
        if (stage == null && btnBrowsePhoto != null && btnBrowsePhoto.getScene() != null) {
            stage = (Stage) btnBrowsePhoto.getScene().getWindow();
        }

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            if (txtPhotoUrl != null) {
                txtPhotoUrl.setText(selectedFile.getAbsolutePath());
            }
            updatePreviewPhoto(selectedFile.getAbsolutePath());
        }
    }

    private void updatePreviewPhoto(String photoPath) {
        if (imgPreview == null) {
            return;
        }

        Image image = null;
        if (photoPath != null && !photoPath.isBlank()) {
            File f = new File(photoPath);
            if (f.exists()) {
                try {
                    image = new Image(f.toURI().toString());
                } catch (Exception ignored) {}
            }
        }

        if (image == null || image.isError()) {
            InputStream defaultStream = getClass().getResourceAsStream("/com/omnidrive/view/images/default_vehicle.png");
            if (defaultStream != null) {
                image = new Image(defaultStream);
            }
        }

        if (image != null) {
            imgPreview.setImage(image);
        }
    }

    private void attachErrorClearingListeners() {
        TextField[] fields = {txtPlate, txtBrand, txtModelYear, txtSpeedCount, txtMaxSpeed,
                txtCylinderCapacity, txtBasePrice, txtAutonomyKm, txtChargingHours};
        for (TextField tf : fields) {
            if (tf != null) {
                tf.textProperty().addListener((obs, oldV, newV) -> setFieldError(tf, false));
            }
        }
    }

    private void setFieldError(TextField field, boolean hasError) {
        if (field == null) return;
        if (hasError) {
            if (!field.getStyleClass().contains("text-field-error")) {
                field.getStyleClass().add("text-field-error");
            }
        } else {
            field.getStyleClass().remove("text-field-error");
        }
    }

    public Vehicle processSaveVehicle() {
        List<String> errors = new ArrayList<>();

        String plate = getTrimmedText(txtPlate);
        ValidationResult resPlate = InputValidator.validatePlate(plate);
        setFieldError(txtPlate, !resPlate.isValid());
        if (!resPlate.isValid()) errors.add(resPlate.getFirstErrorMessage());

        String brand = getTrimmedText(txtBrand);
        ValidationResult resBrand = InputValidator.validateName(brand, "Marca");
        setFieldError(txtBrand, !resBrand.isValid());
        if (!resBrand.isValid()) errors.add(resBrand.getFirstErrorMessage());

        ValidationResult resYear = InputValidator.parsePositiveInt(getTrimmedText(txtModelYear), "Año de Modelo");
        int modelYear = 0;
        if (resYear.isValid()) {
            modelYear = Integer.parseInt(getTrimmedText(txtModelYear));
            ValidationResult resYearRange = InputValidator.validateModelYear(modelYear);
            if (!resYearRange.isValid()) {
                resYear = resYearRange;
            }
        }
        setFieldError(txtModelYear, !resYear.isValid());
        if (!resYear.isValid()) errors.add(resYear.getFirstErrorMessage());

        ValidationResult resSpeedCount = InputValidator.parsePositiveInt(getTrimmedText(txtSpeedCount), "Número de Cambios");
        setFieldError(txtSpeedCount, !resSpeedCount.isValid());
        if (!resSpeedCount.isValid()) errors.add(resSpeedCount.getFirstErrorMessage());

        ValidationResult resMaxSpeed = InputValidator.parsePositiveDouble(getTrimmedText(txtMaxSpeed), "Velocidad Máxima");
        setFieldError(txtMaxSpeed, !resMaxSpeed.isValid());
        if (!resMaxSpeed.isValid()) errors.add(resMaxSpeed.getFirstErrorMessage());

        ValidationResult resCylinder = InputValidator.parsePositiveDouble(getTrimmedText(txtCylinderCapacity), "Cilindraje");
        setFieldError(txtCylinderCapacity, !resCylinder.isValid());
        if (!resCylinder.isValid()) errors.add(resCylinder.getFirstErrorMessage());

        ValidationResult resPrice = InputValidator.parsePositiveDouble(getTrimmedText(txtBasePrice), "Precio Base");
        setFieldError(txtBasePrice, !resPrice.isValid());
        if (!resPrice.isValid()) errors.add(resPrice.getFirstErrorMessage());

        FuelType fuel = cmbFuelType != null ? cmbFuelType.getValue() : FuelType.GASOLINE;
        if (fuel == FuelType.ELECTRIC) {
            ValidationResult resAutonomy = InputValidator.parsePositiveDouble(getTrimmedText(txtAutonomyKm), "Autonomía (km)");
            setFieldError(txtAutonomyKm, !resAutonomy.isValid());
            if (!resAutonomy.isValid()) errors.add(resAutonomy.getFirstErrorMessage());

            ValidationResult resCharge = InputValidator.parsePositiveDouble(getTrimmedText(txtChargingHours), "Tiempo de Carga (horas)");
            setFieldError(txtChargingHours, !resCharge.isValid());
            if (!resCharge.isValid()) errors.add(resCharge.getFirstErrorMessage());
        }

        if (!errors.isEmpty()) {
            AlertUtil.showError("Datos de Vehículo Inválidos", String.join("\n", errors));
            return null;
        }

        try {
            ElectricDetails electricDetails = null;
            if (fuel == FuelType.ELECTRIC) {
                double autonomy = Double.parseDouble(getTrimmedText(txtAutonomyKm).replace(",", "."));
                double chargingHours = Double.parseDouble(getTrimmedText(txtChargingHours).replace(",", "."));
                electricDetails = new ElectricDetails(autonomy, chargingHours);
            }
            int speedCount = Integer.parseInt(getTrimmedText(txtSpeedCount));
            double maxSpeed = Double.parseDouble(getTrimmedText(txtMaxSpeed).replace(",", "."));
            double cylinderCapacity = Double.parseDouble(getTrimmedText(txtCylinderCapacity).replace(",", "."));
            TransmissionType transmission = cmbTransmission != null ? cmbTransmission.getValue() : TransmissionType.AUTOMATIC;
            VehicleCondition condition = cmbCondition != null ? cmbCondition.getValue() : VehicleCondition.NEW;
            VehicleStatus status = cmbStatus != null ? cmbStatus.getValue() : VehicleStatus.AVAILABLE_FOR_SALE;
            double basePrice = Double.parseDouble(getTrimmedText(txtBasePrice).replace(",", "."));
            String photoUrl = txtPhotoUrl != null ? txtPhotoUrl.getText() : null;
            boolean isTechnicalReviewApproved = chkTechnicalReview != null && chkTechnicalReview.isSelected();

            HybridDetails hybridDetails = null;
            if (fuel == FuelType.HYBRID) {
                boolean plugIn = chkPlugIn != null && chkPlugIn.isSelected();
                boolean mildHybrid = chkMildHybrid != null && chkMildHybrid.isSelected();
                hybridDetails = new HybridDetails(plugIn, mildHybrid);
            }

            String type = cmbVehicleType != null ? cmbVehicleType.getValue() : "Sedán";
            Vehicle createdVehicle = buildVehicleInstance(type, plate, brand, modelYear, speedCount, maxSpeed,
                    cylinderCapacity, fuel, transmission, condition, status, basePrice, photoUrl,
                    isTechnicalReviewApproved, electricDetails, hybridDetails);

            AppContext.getDealership().registerVehicle(createdVehicle);
            AppContext.save();

            if (onVehicleRegistered != null) {
                onVehicleRegistered.accept(createdVehicle);
            }

            closeDialog();
            return createdVehicle;
        } catch (OmniDriveException e) {
            AlertUtil.showError("Error al Registrar Vehículo", e.getMessage());
            return null;
        }
    }

    private Vehicle buildVehicleInstance(String type, String plate, String brand, int modelYear, int speedCount,
                                         double maxSpeed, double cylinderCapacity, FuelType fuel,
                                         TransmissionType transmission, VehicleCondition condition,
                                         VehicleStatus status, double basePrice, String photoUrl,
                                         boolean isReviewApproved, ElectricDetails electricDetails,
                                         HybridDetails hybridDetails) throws OmniDriveException {
        return switch (type) {
            case "Moto" -> new Motorcycle(plate, brand, modelYear, speedCount, maxSpeed, cylinderCapacity,
                    fuel, transmission, condition, status, basePrice, photoUrl, isReviewApproved,
                    electricDetails, hybridDetails);

            case "Sedán" -> new Sedan(plate, brand, modelYear, speedCount, maxSpeed, cylinderCapacity,
                    fuel, transmission, condition, status, basePrice, photoUrl, isReviewApproved,
                    electricDetails, hybridDetails,
                    getInt(txtPassengerCount, 5), getInt(txtDoorsCount, 4), getInt(txtAirbagsCount, 6),
                    getBool(chkAirConditioning), getBool(chkReverseCamera), getBool(chkAbs),
                    getDouble(txtTrunkCapacity, 450.0), getBool(chkCruiseControl), getBool(chkCollisionSensors),
                    getBool(chkCrossTraffic), getBool(chkLaneAssist));

            case "Deportivo" -> new SportsCar(plate, brand, modelYear, speedCount, maxSpeed, cylinderCapacity,
                    fuel, transmission, condition, status, basePrice, photoUrl, isReviewApproved,
                    electricDetails, hybridDetails,
                    getInt(txtPassengerCount, 2), getInt(txtDoorsCount, 2), getInt(txtAirbagsCount, 6),
                    getInt(txtHorsepower, 400), getDouble(txtZeroToHundred, 3.8));

            case "Camioneta / SUV" -> new Suv(plate, brand, modelYear, speedCount, maxSpeed, cylinderCapacity,
                    fuel, transmission, condition, status, basePrice, photoUrl, isReviewApproved,
                    electricDetails, hybridDetails,
                    getInt(txtPassengerCount, 7), getInt(txtDoorsCount, 5), getInt(txtAirbagsCount, 6),
                    getBool(chkAirConditioning), getBool(chkReverseCamera), getBool(chkAbs),
                    getDouble(txtTrunkCapacity, 600.0), getBool(chkCruiseControl), getBool(chkCollisionSensors),
                    getBool(chkCrossTraffic), getBool(chkLaneAssist), getBool(chkFourByFour));

            case "Pick Up" -> new PickUp(plate, brand, modelYear, speedCount, maxSpeed, cylinderCapacity,
                    fuel, transmission, condition, status, basePrice, photoUrl, isReviewApproved,
                    electricDetails, hybridDetails,
                    getInt(txtPassengerCount, 5), getInt(txtDoorsCount, 4), getInt(txtAirbagsCount, 4),
                    getBool(chkAirConditioning), getBool(chkReverseCamera), getBool(chkAbs),
                    getBool(chkFourByFour), getDouble(txtBedCapacity, 1000.0));

            case "Van" -> new Van(plate, brand, modelYear, speedCount, maxSpeed, cylinderCapacity,
                    fuel, transmission, condition, status, basePrice, photoUrl, isReviewApproved,
                    electricDetails, hybridDetails,
                    getInt(txtPassengerCount, 12), getInt(txtDoorsCount, 4), getInt(txtAirbagsCount, 4),
                    getBool(chkAirConditioning), getBool(chkReverseCamera), getBool(chkAbs),
                    getDouble(txtTrunkCapacity, 800.0));

            case "Bus" -> new Bus(plate, brand, modelYear, speedCount, maxSpeed, cylinderCapacity,
                    fuel, transmission, condition, status, basePrice, photoUrl, isReviewApproved,
                    electricDetails, hybridDetails,
                    getInt(txtPassengerCount, 40), getInt(txtDoorsCount, 2), getInt(txtAirbagsCount, 4),
                    getBool(chkAirConditioning), getBool(chkReverseCamera), getBool(chkAbs),
                    getDouble(txtTrunkCapacity, 1500.0), getInt(txtAxleCount, 3), getInt(txtEmergencyExits, 2));

            case "Camión" -> new Truck(plate, brand, modelYear, speedCount, maxSpeed, cylinderCapacity,
                    fuel, transmission, condition, status, basePrice, photoUrl, isReviewApproved,
                    electricDetails, hybridDetails,
                    getDouble(txtLoadCapacityTons, 15.0), getBool(chkTruckAirConditioning),
                    getBool(chkTruckAirBrakes), getBool(chkTruckAbs), getInt(txtTruckAxles, 3),
                    cmbTruckType != null && cmbTruckType.getValue() != null ? cmbTruckType.getValue() : TruckType.BOX_TRUCK);

            default -> throw new OmniDriveException("Tipo de vehículo no reconocido: " + type);
        };
    }

    private String getTrimmedText(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }

    private int getInt(TextField tf, int defaultVal) {
        if (tf != null && tf.getText() != null && !tf.getText().isBlank()) {
            ValidationResult res = InputValidator.parsePositiveInt(tf.getText(), "Campo");
            if (res.isValid()) {
                return Integer.parseInt(tf.getText().trim());
            }
        }
        return defaultVal;
    }

    private double getDouble(TextField tf, double defaultVal) {
        if (tf != null && tf.getText() != null && !tf.getText().isBlank()) {
            ValidationResult res = InputValidator.parsePositiveDouble(tf.getText(), "Campo");
            if (res.isValid()) {
                return Double.parseDouble(tf.getText().trim().replace(",", "."));
            }
        }
        return defaultVal;
    }

    private boolean getBool(CheckBox cb) {
        return cb != null && cb.isSelected();
    }

    @FXML
    private void handleSave() {
        Vehicle v = processSaveVehicle();
        if (v != null) {
            AlertUtil.showInfo("Registro Exitoso", "El vehículo con placa " + v.getPlate() + " fue registrado en el inventario.");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        } else if (btnCancel != null && btnCancel.getScene() != null && btnCancel.getScene().getWindow() != null) {
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }

    // Getters y Setters para pruebas unitarias e integración

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setOnVehicleRegistered(Consumer<Vehicle> onVehicleRegistered) {
        this.onVehicleRegistered = onVehicleRegistered;
    }

    public ComboBox<String> getCmbVehicleType() { return cmbVehicleType; }
    public void setCmbVehicleType(ComboBox<String> cmbVehicleType) { this.cmbVehicleType = cmbVehicleType; }

    public TextField getTxtPlate() { return txtPlate; }
    public void setTxtPlate(TextField txtPlate) { this.txtPlate = txtPlate; }

    public TextField getTxtBrand() { return txtBrand; }
    public void setTxtBrand(TextField txtBrand) { this.txtBrand = txtBrand; }

    public TextField getTxtModelYear() { return txtModelYear; }
    public void setTxtModelYear(TextField txtModelYear) { this.txtModelYear = txtModelYear; }

    public TextField getTxtSpeedCount() { return txtSpeedCount; }
    public void setTxtSpeedCount(TextField txtSpeedCount) { this.txtSpeedCount = txtSpeedCount; }

    public TextField getTxtMaxSpeed() { return txtMaxSpeed; }
    public void setTxtMaxSpeed(TextField txtMaxSpeed) { this.txtMaxSpeed = txtMaxSpeed; }

    public TextField getTxtCylinderCapacity() { return txtCylinderCapacity; }
    public void setTxtCylinderCapacity(TextField txtCylinderCapacity) { this.txtCylinderCapacity = txtCylinderCapacity; }

    public ComboBox<TransmissionType> getCmbTransmission() { return cmbTransmission; }
    public void setCmbTransmission(ComboBox<TransmissionType> cmbTransmission) { this.cmbTransmission = cmbTransmission; }

    public ComboBox<FuelType> getCmbFuelType() { return cmbFuelType; }
    public void setCmbFuelType(ComboBox<FuelType> cmbFuelType) { this.cmbFuelType = cmbFuelType; }

    public ComboBox<VehicleCondition> getCmbCondition() { return cmbCondition; }
    public void setCmbCondition(ComboBox<VehicleCondition> cmbCondition) { this.cmbCondition = cmbCondition; }

    public ComboBox<VehicleStatus> getCmbStatus() { return cmbStatus; }
    public void setCmbStatus(ComboBox<VehicleStatus> cmbStatus) { this.cmbStatus = cmbStatus; }

    public TextField getTxtBasePrice() { return txtBasePrice; }
    public void setTxtBasePrice(TextField txtBasePrice) { this.txtBasePrice = txtBasePrice; }

    public TextField getTxtPhotoUrl() { return txtPhotoUrl; }
    public void setTxtPhotoUrl(TextField txtPhotoUrl) { this.txtPhotoUrl = txtPhotoUrl; }

    public CheckBox getChkTechnicalReview() { return chkTechnicalReview; }
    public void setChkTechnicalReview(CheckBox chkTechnicalReview) { this.chkTechnicalReview = chkTechnicalReview; }

    public ImageView getImgPreview() { return imgPreview; }
    public void setImgPreview(ImageView imgPreview) { this.imgPreview = imgPreview; }

    public Button getBtnBrowsePhoto() { return btnBrowsePhoto; }
    public void setBtnBrowsePhoto(Button btnBrowsePhoto) { this.btnBrowsePhoto = btnBrowsePhoto; }

    public VBox getPaneElectric() { return paneElectric; }
    public void setPaneElectric(VBox paneElectric) { this.paneElectric = paneElectric; }

    public TextField getTxtAutonomyKm() { return txtAutonomyKm; }
    public void setTxtAutonomyKm(TextField txtAutonomyKm) { this.txtAutonomyKm = txtAutonomyKm; }

    public TextField getTxtChargingHours() { return txtChargingHours; }
    public void setTxtChargingHours(TextField txtChargingHours) { this.txtChargingHours = txtChargingHours; }

    public VBox getPaneHybrid() { return paneHybrid; }
    public void setPaneHybrid(VBox paneHybrid) { this.paneHybrid = paneHybrid; }

    public CheckBox getChkPlugIn() { return chkPlugIn; }
    public void setChkPlugIn(CheckBox chkPlugIn) { this.chkPlugIn = chkPlugIn; }

    public CheckBox getChkMildHybrid() { return chkMildHybrid; }
    public void setChkMildHybrid(CheckBox chkMildHybrid) { this.chkMildHybrid = chkMildHybrid; }

    public VBox getPanePassenger() { return panePassenger; }
    public void setPanePassenger(VBox panePassenger) { this.panePassenger = panePassenger; }

    public TextField getTxtPassengerCount() { return txtPassengerCount; }
    public void setTxtPassengerCount(TextField txtPassengerCount) { this.txtPassengerCount = txtPassengerCount; }

    public TextField getTxtDoorsCount() { return txtDoorsCount; }
    public void setTxtDoorsCount(TextField txtDoorsCount) { this.txtDoorsCount = txtDoorsCount; }

    public TextField getTxtAirbagsCount() { return txtAirbagsCount; }
    public void setTxtAirbagsCount(TextField txtAirbagsCount) { this.txtAirbagsCount = txtAirbagsCount; }

    public CheckBox getChkAirConditioning() { return chkAirConditioning; }
    public void setChkAirConditioning(CheckBox chkAirConditioning) { this.chkAirConditioning = chkAirConditioning; }

    public CheckBox getChkReverseCamera() { return chkReverseCamera; }
    public void setChkReverseCamera(CheckBox chkReverseCamera) { this.chkReverseCamera = chkReverseCamera; }

    public CheckBox getChkAbs() { return chkAbs; }
    public void setChkAbs(CheckBox chkAbs) { this.chkAbs = chkAbs; }

    public VBox getPaneTrunk() { return paneTrunk; }
    public void setPaneTrunk(VBox paneTrunk) { this.paneTrunk = paneTrunk; }

    public TextField getTxtTrunkCapacity() { return txtTrunkCapacity; }
    public void setTxtTrunkCapacity(TextField txtTrunkCapacity) { this.txtTrunkCapacity = txtTrunkCapacity; }

    public VBox getPaneSedanSuv() { return paneSedanSuv; }
    public void setPaneSedanSuv(VBox paneSedanSuv) { this.paneSedanSuv = paneSedanSuv; }

    public CheckBox getChkCruiseControl() { return chkCruiseControl; }
    public void setChkCruiseControl(CheckBox chkCruiseControl) { this.chkCruiseControl = chkCruiseControl; }

    public CheckBox getChkCollisionSensors() { return chkCollisionSensors; }
    public void setChkCollisionSensors(CheckBox chkCollisionSensors) { this.chkCollisionSensors = chkCollisionSensors; }

    public CheckBox getChkCrossTraffic() { return chkCrossTraffic; }
    public void setChkCrossTraffic(CheckBox chkCrossTraffic) { this.chkCrossTraffic = chkCrossTraffic; }

    public CheckBox getChkLaneAssist() { return chkLaneAssist; }
    public void setChkLaneAssist(CheckBox chkLaneAssist) { this.chkLaneAssist = chkLaneAssist; }

    public VBox getPaneFourByFour() { return paneFourByFour; }
    public void setPaneFourByFour(VBox paneFourByFour) { this.paneFourByFour = paneFourByFour; }

    public CheckBox getChkFourByFour() { return chkFourByFour; }
    public void setChkFourByFour(CheckBox chkFourByFour) { this.chkFourByFour = chkFourByFour; }

    public VBox getPanePickUp() { return panePickUp; }
    public void setPanePickUp(VBox panePickUp) { this.panePickUp = panePickUp; }

    public TextField getTxtBedCapacity() { return txtBedCapacity; }
    public void setTxtBedCapacity(TextField txtBedCapacity) { this.txtBedCapacity = txtBedCapacity; }

    public VBox getPaneSportsCar() { return paneSportsCar; }
    public void setPaneSportsCar(VBox paneSportsCar) { this.paneSportsCar = paneSportsCar; }

    public TextField getTxtHorsepower() { return txtHorsepower; }
    public void setTxtHorsepower(TextField txtHorsepower) { this.txtHorsepower = txtHorsepower; }

    public TextField getTxtZeroToHundred() { return txtZeroToHundred; }
    public void setTxtZeroToHundred(TextField txtZeroToHundred) { this.txtZeroToHundred = txtZeroToHundred; }

    public VBox getPaneBus() { return paneBus; }
    public void setPaneBus(VBox paneBus) { this.paneBus = paneBus; }

    public TextField getTxtAxleCount() { return txtAxleCount; }
    public void setTxtAxleCount(TextField txtAxleCount) { this.txtAxleCount = txtAxleCount; }

    public TextField getTxtEmergencyExits() { return txtEmergencyExits; }
    public void setTxtEmergencyExits(TextField txtEmergencyExits) { this.txtEmergencyExits = txtEmergencyExits; }

    public VBox getPaneTruck() { return paneTruck; }
    public void setPaneTruck(VBox paneTruck) { this.paneTruck = paneTruck; }

    public TextField getTxtLoadCapacityTons() { return txtLoadCapacityTons; }
    public void setTxtLoadCapacityTons(TextField txtLoadCapacityTons) { this.txtLoadCapacityTons = txtLoadCapacityTons; }

    public CheckBox getChkTruckAirBrakes() { return chkTruckAirBrakes; }
    public void setChkTruckAirBrakes(CheckBox chkTruckAirBrakes) { this.chkTruckAirBrakes = chkTruckAirBrakes; }

    public CheckBox getChkTruckAirConditioning() { return chkTruckAirConditioning; }
    public void setChkTruckAirConditioning(CheckBox chkTruckAirConditioning) { this.chkTruckAirConditioning = chkTruckAirConditioning; }

    public CheckBox getChkTruckAbs() { return chkTruckAbs; }
    public void setChkTruckAbs(CheckBox chkTruckAbs) { this.chkTruckAbs = chkTruckAbs; }

    public TextField getTxtTruckAxles() { return txtTruckAxles; }
    public void setTxtTruckAxles(TextField txtTruckAxles) { this.txtTruckAxles = txtTruckAxles; }

    public ComboBox<TruckType> getCmbTruckType() { return cmbTruckType; }
    public void setCmbTruckType(ComboBox<TruckType> cmbTruckType) { this.cmbTruckType = cmbTruckType; }

    public Button getBtnSave() { return btnSave; }
    public void setBtnSave(Button btnSave) { this.btnSave = btnSave; }

    public Button getBtnCancel() { return btnCancel; }
    public void setBtnCancel(Button btnCancel) { this.btnCancel = btnCancel; }
}
