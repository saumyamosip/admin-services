package io.mosip.registration.controller.reg;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.IdValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.IntroducerType;
import io.mosip.registration.constants.ProcessNames;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.VirtualKeyboard;
import io.mosip.registration.controller.auth.AuthenticationController;
import io.mosip.registration.controller.device.WebCameraController;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.OSIDataDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.RegistrationMetaDataDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.dto.biometric.BiometricDTO;
import io.mosip.registration.dto.biometric.BiometricInfoDTO;
import io.mosip.registration.dto.demographic.AddressDTO;
import io.mosip.registration.dto.demographic.ApplicantDocumentDTO;
import io.mosip.registration.dto.demographic.DemographicDTO;
import io.mosip.registration.dto.demographic.DemographicInfoDTO;
import io.mosip.registration.dto.demographic.LocationDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.external.PreRegZipHandlingService;
import io.mosip.registration.service.sync.PreRegistrationDataSyncService;
import io.mosip.registration.util.dataprovider.DataProvider;
import io.mosip.registration.util.kernal.RIDGenerator;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Class for Registration Page Controller
 * 
 * @author Taleev.Aalam
 * @since 1.0.0
 *
 */

@Controller
public class RegistrationController extends BaseController {

	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(RegistrationController.class);

	@Autowired
	private DocumentScanController documentScanController;

	@Autowired
	private AuthenticationController authenticationController;
	@FXML
	private TextField preRegistrationId;

	@FXML
	private TextField fullName;

	@FXML
	private TextField fullNameLocalLanguage;

	@FXML
	private Label fullNameLocalLanguageLabel;

	@FXML
	private DatePicker ageDatePicker;

	private DatePicker autoAgeDatePicker = new DatePicker();

	@FXML
	private TextField ageField;

	@FXML
	private Label bioExceptionToggleLabel1;

	@FXML
	private Label bioExceptionToggleLabel2;

	@FXML
	private Label toggleLabel1;

	@FXML
	private Label toggleLabel2;

	@FXML
	private AnchorPane childSpecificFields;

	private SimpleBooleanProperty switchedOn;

	private SimpleBooleanProperty switchedOnForBiometricException;

	@FXML
	private ComboBox<String> gender;

	@FXML
	private TextField addressLine1;

	@FXML
	private TextField addressLine1LocalLanguage;

	@FXML
	private Label addressLine1LocalLanguagelabel;

	@FXML
	private TextField addressLine2;

	@FXML
	private TextField addressLine2LocalLanguage;

	@FXML
	private Label addressLine2LocalLanguagelabel;

	@FXML
	private TextField addressLine3;

	@FXML
	private TextField addressLine3LocalLanguage;

	@FXML
	private Label addressLine3LocalLanguagelabel;

	@FXML
	private TextField emailId;

	@FXML
	private TextField mobileNo;

	@FXML
	private TextField region;

	@FXML
	private TextField city;

	@FXML
	private TextField province;

	@FXML
	private TextField postalCode;

	@FXML
	private TextField localAdminAuthority;

	@FXML
	private TextField cniOrPinNumber;

	@FXML
	private TextField parentName;

	@FXML
	private TextField uinId;

	@FXML
	private TitledPane demoGraphicTitlePane;

	@FXML
	private TitledPane biometricTitlePane;

	@FXML
	private Accordion accord;

	@FXML
	private AnchorPane demoGraphicPane1;

	@FXML
	private ImageView headerImage;

	@FXML
	private Button nextBtn;

	@FXML
	private Button pane2NextBtn;

	@FXML
	private VBox demoGraphicVBox;

	@FXML
	private AnchorPane demoGraphicPane2;

	@FXML
	private AnchorPane anchorPaneRegistration;

	@FXML
	private Label copyAddressLabel;

	@FXML
	private ImageView copyAddressImage;

	private boolean toggleAgeOrDobField;

	private boolean isChild;

	private Node keyboardNode;

	@Value("${capture_photo_using_device}")
	public String capturePhotoUsingDevice;

	@FXML
	private AnchorPane biometricsPane;
	@FXML
	protected ImageView applicantImage;
	@FXML
	protected ImageView exceptionImage;
	@FXML
	protected Button captureImage;
	@FXML
	protected Button captureExceptionImage;
	@FXML
	protected Button saveBiometricDetailsBtn;
	@FXML
	protected Button biometricPrevBtn;
	@FXML
	protected Button pane2PrevBtn;
	@FXML
	protected Button autoFillBtn;
	@FXML
	protected Button fetchBtn;

	@FXML
	private AnchorPane fingerPrintCapturePane;
	@FXML
	private AnchorPane irisCapture;

	private BufferedImage applicantBufferedImage;
	private BufferedImage exceptionBufferedImage;

	private boolean applicantImageCaptured;
	private boolean exceptionImageCaptured;

	private boolean toggleBiometricException;

	private Image defaultImage;

	@FXML
	private TitledPane authenticationTitlePane;

	@Autowired
	private PreRegZipHandlingService preRegZipHandlingService;

	@Autowired
	private PreRegistrationDataSyncService preRegistrationDataSyncService;

	@Autowired
	private WebCameraController webCameraController;

	private boolean dobSelectionFromCalendar = true;

	@Autowired
	private IdValidator<String> pridValidatorImpl;

	@Autowired
	private Validations validation;

	@FXML
	private void initialize() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Entering the LOGIN_CONTROLLER");
		try {
			auditFactory.audit(AuditEvent.GET_REGISTRATION_CONTROLLER, Components.REGISTRATION_CONTROLLER,
					"initializing the registration controller",
					SessionContext.getInstance().getUserContext().getUserId(),
					RegistrationConstants.ONBOARD_DEVICES_REF_ID_TYPE);

			// Create RegistrationDTO Object
			if (getRegistrationDtoContent() == null) {
				createRegistrationDTOObject();
			}

			if (capturePhotoUsingDevice.equals("Y") && !isEditPage()) {
				defaultImage = applicantImage.getImage();
				applicantImageCaptured = false;
				exceptionImageCaptured = false;
				exceptionBufferedImage = null;
			}

			demoGraphicTitlePane.expandedProperty().addListener(
					(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
						if (newValue) {
							headerImage.setImage(new Image(RegistrationConstants.DEMOGRAPHIC_DETAILS_LOGO));
						}
					});
			biometricTitlePane.expandedProperty().addListener(
					(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
						if (newValue) {
							headerImage.setImage(new Image(RegistrationConstants.APPLICANT_BIOMETRICS_LOGO));
						}
					});
			authenticationTitlePane.expandedProperty().addListener(
					(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
						if (newValue) {
							headerImage.setImage(new Image(RegistrationConstants.OPERATOR_AUTHENTICATION_LOGO));
						}
					});

			switchedOn = new SimpleBooleanProperty(false);
			switchedOnForBiometricException = new SimpleBooleanProperty(false);
			toggleAgeOrDobField = false;
			isChild = true;
			ageDatePicker.setDisable(false);
			ageField.setDisable(true);
			accord.setExpandedPane(demoGraphicTitlePane);
			disableFutureDays();
			toggleFunction();
			toggleFunctionForBiometricException();
			ageFieldValidations();
			ageValidationInDatePicker();
			dateFormatter();
			populateTheLocalLangFields();
			loadLanguageSpecificKeyboard();
			loadLocalLanguageFields();
			// loadListOfDocuments();
			// setScrollFalse();
			loadKeyboard();
			if (isEditPage() && getRegistrationDtoContent() != null) {
				prepareEditPageContent();
			}
		} catch (IOException | RuntimeException exception) {
			LOGGER.error("REGISTRATION - CONTROLLER", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					exception.getMessage());
			generateAlert(RegistrationConstants.ALERT_ERROR, RegistrationConstants.UNABLE_LOAD_REG_PAGE);
		}
	}

	/**
	 * Loading the virtual keyboard
	 */
	private void loadKeyboard() {
		try {
			VirtualKeyboard vk = VirtualKeyboard.getInstance();
			keyboardNode = vk.view();
			demoGraphicPane1.getChildren().add(keyboardNode);
			keyboardNode.setVisible(false);
			vk.changeControlOfKeyboard(fullNameLocalLanguage);
			vk.changeControlOfKeyboard(addressLine1LocalLanguage);
			vk.changeControlOfKeyboard(addressLine2LocalLanguage);
			vk.changeControlOfKeyboard(addressLine3LocalLanguage);
		} catch (NullPointerException exception) {
			LOGGER.error("REGISTRATION - CONTROLLER", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					exception.getMessage());
		}
	}

	/**
	 * This method is to prepopulate all the values for edit operation
	 */
	private void prepareEditPageContent() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Preparing the Edit page content");

			DemographicDTO demographicDTO = getRegistrationDtoContent().getDemographicDTO();
			DemographicInfoDTO demographicInfoDTO = demographicDTO.getDemoInUserLang();
			AddressDTO addressDTO = demographicInfoDTO.getAddressDTO();
			LocationDTO locationDTO = addressDTO.getLocationDTO();

			fullName.setText(demographicInfoDTO.getFullName());

			if (demographicInfoDTO.getDateOfBirth() != null && getAgeDatePickerContent() != null
					&& dobSelectionFromCalendar) {
				ageDatePicker.setValue(getAgeDatePickerContent().getValue());
			} else {
				switchedOn.set(true);
				ageDatePicker.setDisable(true);
				ageField.setDisable(false);
				ageField.setText(demographicInfoDTO.getAge());
				if (isEditPage())
					autoAgeDatePicker.setValue(getAgeDatePickerContent().getValue());
			}

			gender.setValue(demographicInfoDTO.getGender());

			addressLine1.setText(addressDTO.getAddressLine1());
			addressLine2.setText(addressDTO.getAddressLine2());
			addressLine3.setText(addressDTO.getAddressLine3());

			province.setText(locationDTO.getProvince());
			city.setText(locationDTO.getCity());
			region.setText(locationDTO.getRegion());
			postalCode.setText(locationDTO.getPostalCode());

			mobileNo.setText(demographicInfoDTO.getMobile());
			emailId.setText(demographicInfoDTO.getEmailId());
			cniOrPinNumber.setText(demographicInfoDTO.getCneOrPINNumber());
			localAdminAuthority.setText(demographicInfoDTO.getLocalAdministrativeAuthority());

			if (demographicDTO.getIntroducerRID() != null) {
				uinId.setText(demographicDTO.getIntroducerRID());
			}
			if (demographicDTO.getIntroducerUIN() != null) {
				uinId.setText(demographicDTO.getIntroducerUIN());
			}
			if (demographicInfoDTO.getParentOrGuardianName() != null) {
				parentName.setText(demographicInfoDTO.getParentOrGuardianName());
			}
			preRegistrationId.setText(getRegistrationDtoContent().getPreRegistrationId());

			// for applicant biometrics
			if (getRegistrationDtoContent().getDemographicDTO().getApplicantDocumentDTO() != null) {
				if (getRegistrationDtoContent().getDemographicDTO().getApplicantDocumentDTO().getPhoto() != null) {
					byte[] photoInBytes = getRegistrationDtoContent().getDemographicDTO().getApplicantDocumentDTO()
							.getPhoto();
					if (photoInBytes != null) {
						ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(photoInBytes);
						applicantImage.setImage(new Image(byteArrayInputStream));
					}
				}
				if (getRegistrationDtoContent().getDemographicDTO().getApplicantDocumentDTO()
						.getExceptionPhoto() != null) {
					byte[] exceptionPhotoInBytes = getRegistrationDtoContent().getDemographicDTO()
							.getApplicantDocumentDTO().getExceptionPhoto();
					if (exceptionPhotoInBytes != null) {
						ByteArrayInputStream inputStream = new ByteArrayInputStream(exceptionPhotoInBytes);
						exceptionImage.setImage(new Image(inputStream));
					}
				}
			}

			documentScanController.prepareEditPageContent();
			SessionContext.getInstance().getMapObject().put(RegistrationConstants.REGISTRATION_ISEDIT, false);
			ageFieldValidations();
			ageValidationInDatePicker();

		} catch (RuntimeException runtimeException) {
			LOGGER.error(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}

	}

	@FXML
	private void fetchPreRegistration() {
		String preRegId = preRegistrationId.getText();

		if (StringUtils.isEmpty(preRegId)) {
			generateAlert(RegistrationConstants.ALERT_ERROR, RegistrationConstants.PRE_REG_ID_EMPTY);
			return;
		} else {
			try {
				pridValidatorImpl.validateId(preRegId);
			} catch (InvalidIDException invalidIDException) {
				generateAlert(RegistrationConstants.ALERT_ERROR, invalidIDException.getErrorText());
				return;
			}
		}
		ResponseDTO responseDTO = preRegistrationDataSyncService.getPreRegistration(preRegId);

		SuccessResponseDTO successResponseDTO = responseDTO.getSuccessResponseDTO();
		List<ErrorResponseDTO> errorResponseDTOList = responseDTO.getErrorResponseDTOs();

		if (successResponseDTO != null && successResponseDTO.getOtherAttributes() != null
				&& successResponseDTO.getOtherAttributes().containsKey("registrationDto")) {
			SessionContext.getInstance().getMapObject().put(RegistrationConstants.REGISTRATION_DATA,
					successResponseDTO.getOtherAttributes().get("registrationDto"));
			prepareEditPageContent();

		} else if (errorResponseDTOList != null && !errorResponseDTOList.isEmpty()) {
			generateAlert(RegistrationConstants.ALERT_ERROR, errorResponseDTOList.get(0).getMessage());
		}
	}

	/**
	 * 
	 * Loading the address detail from previous entry
	 * 
	 */
	@FXML
	private void loadAddressFromPreviousEntry() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Loading address from previous entry");
			if (SessionContext.getInstance().getMapObject().get(RegistrationConstants.ADDRESS_KEY) == null) {
				generateAlert(RegistrationConstants.ALERT_ERROR,
						"Address could not be loaded as there is no previous entry");
				LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
						RegistrationConstants.APPLICATION_ID,
						"Address could not be loaded as there is no previous entry");

			} else {
				LocationDTO locationDto = ((AddressDTO) SessionContext.getInstance().getMapObject()
						.get(RegistrationConstants.ADDRESS_KEY)).getLocationDTO();
				region.setText(locationDto.getRegion());
				city.setText(locationDto.getCity());
				province.setText(locationDto.getProvince());
				postalCode.setText(locationDto.getPostalCode());
				LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
						RegistrationConstants.APPLICATION_ID, "Loaded address from previous entry");
			}
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - LOADING ADDRESS FROM PREVIOUS ENTRY FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * 
	 * Loading the second demographic pane
	 * 
	 */
	@FXML
	private void gotoSecondDemographicPane() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Loading the second demographic pane");

			if (validateDemographicPaneOne()) {
				demoGraphicTitlePane.setContent(null);
				demoGraphicTitlePane.setExpanded(false);
				demoGraphicTitlePane.setContent(demoGraphicPane2);
				demoGraphicTitlePane.setExpanded(true);
				anchorPaneRegistration.setMaxHeight(700);
			}
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - COULD NOT GO TO SECOND DEMOGRAPHIC PANE", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * 
	 * Setting the focus to specific fields when keyboard loads
	 * 
	 */
	@FXML
	private void setFocusonLocalField(MouseEvent event) {
		try {
			keyboardNode.setLayoutX(300.00);
			Node node = (Node) event.getSource();

			if (node.getId().equals("addressLine1")) {
				addressLine1LocalLanguage.requestFocus();
				keyboardNode.setLayoutY(270.00);
			}

			if (node.getId().equals("addressLine2")) {
				addressLine2LocalLanguage.requestFocus();
				keyboardNode.setLayoutY(320.00);
			}

			if (node.getId().equals("addressLine3")) {
				addressLine3LocalLanguage.requestFocus();
				keyboardNode.setLayoutY(375.00);
			}

			if (node.getId().equals("fullName")) {
				fullNameLocalLanguage.requestFocus();
				keyboardNode.setLayoutY(120.00);
			}

			keyboardNode.setVisible(true);
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - SETTING FOCUS ON LOCAL FIELED FAILED", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * 
	 * Saving the detail into concerned DTO'S
	 * 
	 */
	@FXML
	private void saveDetail() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Saving the fields to DTO");
		try {
			auditFactory.audit(AuditEvent.SAVE_DETAIL_TO_DTO, Components.REGISTRATION_CONTROLLER,
					"Saving the details to respected DTO", SessionContext.getInstance().getUserContext().getUserId(),
					RegistrationConstants.ONBOARD_DEVICES_REF_ID_TYPE);

			RegistrationDTO registrationDTO = getRegistrationDtoContent();
			DemographicInfoDTO demographicInfoDTO = new DemographicInfoDTO();
			LocationDTO locationDTO = new LocationDTO();
			AddressDTO addressDTO = new AddressDTO();
			DemographicDTO demographicDTO = registrationDTO.getDemographicDTO();
			OSIDataDTO osiDataDTO = registrationDTO.getOsiDataDTO();
			RegistrationMetaDataDTO registrationMetaDataDTO = registrationDTO.getRegistrationMetaDataDTO();
			if (validateDemographicPaneTwo()) {
				demographicInfoDTO.setFullName(fullName.getText());
				if (ageDatePicker.getValue() != null) {
					dobSelectionFromCalendar = true;
					demographicInfoDTO.setDateOfBirth(Date
							.from(ageDatePicker.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
				} else {
					dobSelectionFromCalendar = false;
					demographicInfoDTO.setDateOfBirth(Date.from(
							autoAgeDatePicker.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
				}
				demographicInfoDTO.setAge(ageField.getText());
				demographicInfoDTO.setGender(gender.getValue());
				addressDTO.setAddressLine1(addressLine1.getText());
				addressDTO.setAddressLine2(addressLine2.getText());
				addressDTO.setLine3(addressLine3.getText());
				locationDTO.setProvince(province.getText());
				locationDTO.setCity(city.getText());
				locationDTO.setRegion(region.getText());
				locationDTO.setPostalCode(postalCode.getText());
				addressDTO.setLocationDTO(locationDTO);
				demographicInfoDTO.setAddressDTO(addressDTO);
				demographicInfoDTO.setMobile(mobileNo.getText());
				demographicInfoDTO.setEmailId(emailId.getText());
				demographicInfoDTO.setChild(isChild);
				demographicInfoDTO.setCneOrPINNumber(cniOrPinNumber.getText());
				demographicInfoDTO.setLocalAdministrativeAuthority(localAdminAuthority.getText());
				if (isChild) {
					if (uinId.getText().length() == Integer.parseInt(AppConfig.getApplicationProperty("uin_length"))) {
						demographicDTO.setIntroducerRID(uinId.getText());
					} else {
						demographicDTO.setIntroducerUIN(uinId.getText());
					}
					osiDataDTO.setIntroducerType(IntroducerType.PARENT.getCode());
					demographicInfoDTO.setParentOrGuardianName(parentName.getText());
					registrationMetaDataDTO.setApplicationType("Child");
				} else {
					registrationMetaDataDTO.setApplicationType("Adult");
				}
				demographicDTO.setDemoInUserLang(demographicInfoDTO);
				osiDataDTO.setOperatorID(SessionContext.getInstance().getUserContext().getUserId());

				// local language
				demographicInfoDTO = new DemographicInfoDTO();
				locationDTO = new LocationDTO();
				addressDTO = new AddressDTO();
				addressDTO.setLocationDTO(locationDTO);
				demographicInfoDTO.setAddressDTO(addressDTO);
				demographicInfoDTO.setFullName(fullNameLocalLanguage.getText());
				addressDTO.setAddressLine1(addressLine1LocalLanguage.getText());
				addressDTO.setAddressLine2(addressLine2LocalLanguage.getText());
				addressDTO.setLine3(addressLine3LocalLanguage.getText());

				demographicDTO.setDemoInLocalLang(demographicInfoDTO);

				registrationDTO.setPreRegistrationId(preRegistrationId.getText());
				registrationDTO.setOsiDataDTO(osiDataDTO);
				registrationDTO.setDemographicDTO(demographicDTO);

				LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
						RegistrationConstants.APPLICATION_ID, "Saved the demographic fields to DTO");

				if (ageDatePicker.getValue() != null) {
					SessionContext.getInstance().getMapObject().put("ageDatePickerContent", ageDatePicker);
				} else {
					SessionContext.getInstance().getMapObject().put("ageDatePickerContent", autoAgeDatePicker);
				}
				biometricTitlePane.setExpanded(true);

			}
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - SAVING THE DETAILS FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	@FXML
	private void goToPreviousPane() {
		try {
			toggleIrisCaptureVisibility(true);
			togglePhotoCaptureVisibility(false);
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - COULD NOT GO TO DEMOGRAPHIC TITLE PANE ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * 
	 * To open camera to capture Applicant Image
	 * 
	 */
	@FXML
	private void openCamForApplicantPhoto() {
		if (webCameraController.webCameraPane == null
				|| !(webCameraController.webCameraPane.getScene().getWindow().isShowing())) {
			openWebCamWindow(RegistrationConstants.APPLICANT_IMAGE);
		}
	}

	/**
	 * 
	 * To open camera to capture Exception Image
	 * 
	 */
	@FXML
	private void openCamForExceptionPhoto() {
		if (webCameraController.webCameraPane == null
				|| !(webCameraController.webCameraPane.getScene().getWindow().isShowing())) {
			openWebCamWindow(RegistrationConstants.EXCEPTION_IMAGE);
		}
	}

	/**
	 * 
	 * To open camera for the type of image that is to be captured
	 * 
	 * @param imageType
	 *            type of image that is to be captured
	 */
	private void openWebCamWindow(String imageType) {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Opening WebCamera to capture photograph");
		try {
			Stage primaryStage = new Stage();
			FXMLLoader loader = BaseController.loadChild(getClass().getResource(RegistrationConstants.WEB_CAMERA_PAGE));
			Parent webCamRoot = loader.load();

			WebCameraController cameraController = loader.getController();
			cameraController.init(this, imageType);

			primaryStage.setTitle(RegistrationConstants.WEB_CAMERA_PAGE_TITLE);
			Scene scene = new Scene(webCamRoot);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException ioException) {
			LOGGER.error(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, ioException.getMessage());
		}
	}

	@Override
	public void saveApplicantPhoto(BufferedImage capturedImage, String photoType) {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Opening WebCamera to capture photograph");

		if (photoType.equals(RegistrationConstants.APPLICANT_IMAGE)) {
			Image capture = SwingFXUtils.toFXImage(capturedImage, null);
			applicantImage.setImage(capture);
			applicantBufferedImage = capturedImage;
			applicantImageCaptured = true;
		} else if (photoType.equals(RegistrationConstants.EXCEPTION_IMAGE)) {
			Image capture = SwingFXUtils.toFXImage(capturedImage, null);
			exceptionImage.setImage(capture);
			exceptionBufferedImage = capturedImage;
			exceptionImageCaptured = true;
		}
	}

	@Override
	public void clearPhoto(String photoType) {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "clearing the image that is captured");

		if (photoType.equals(RegistrationConstants.APPLICANT_IMAGE) && applicantBufferedImage != null) {
			applicantImage.setImage(defaultImage);
			applicantBufferedImage = null;
			applicantImageCaptured = false;
		} else if (photoType.equals(RegistrationConstants.EXCEPTION_IMAGE) && exceptionBufferedImage != null) {
			exceptionImage.setImage(defaultImage);
			exceptionBufferedImage = null;
			exceptionImageCaptured = false;
		}
	}

	@FXML
	private void saveBiometricDetails() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "saving the details of applicant biometrics");

		if (capturePhotoUsingDevice.equals("Y")) {
			if (validateApplicantImage()) {
				try {
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					ImageIO.write(applicantBufferedImage, RegistrationConstants.WEB_CAMERA_IMAGE_TYPE,
							byteArrayOutputStream);
					byte[] photoInBytes = byteArrayOutputStream.toByteArray();
					ApplicantDocumentDTO applicantDocumentDTO = getRegistrationDtoContent().getDemographicDTO()
							.getApplicantDocumentDTO();
					applicantDocumentDTO.setPhoto(photoInBytes);
					applicantDocumentDTO.setPhotographName(RegistrationConstants.APPLICANT_PHOTOGRAPH_NAME);
					byteArrayOutputStream.close();
					if (exceptionBufferedImage != null) {
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						ImageIO.write(exceptionBufferedImage, RegistrationConstants.WEB_CAMERA_IMAGE_TYPE,
								outputStream);
						byte[] exceptionPhotoInBytes = outputStream.toByteArray();
						applicantDocumentDTO.setExceptionPhoto(exceptionPhotoInBytes);
						applicantDocumentDTO.setExceptionPhotoName(RegistrationConstants.EXCEPTION_PHOTOGRAPH_NAME);
						applicantDocumentDTO.setHasExceptionPhoto(true);
						outputStream.close();
					} else {
						applicantDocumentDTO.setHasExceptionPhoto(false);
					}

					LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
							RegistrationConstants.APPLICATION_ID, "showing demographic preview");

					setPreviewContent();
					loadScreen(RegistrationConstants.DEMOGRAPHIC_PREVIEW);
				} catch (IOException ioException) {
					LOGGER.error(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
							RegistrationConstants.APPLICATION_ID, ioException.getMessage());
				}
			}

		} else {
			try {
				DataProvider.setApplicantDocumentDTO(
						getRegistrationDtoContent().getDemographicDTO().getApplicantDocumentDTO(),
						toggleBiometricException);
				setPreviewContent();
				loadScreen(RegistrationConstants.DEMOGRAPHIC_PREVIEW);
			} catch (IOException ioException) {
				LOGGER.error(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
						RegistrationConstants.APPLICATION_ID, ioException.getMessage());
			} catch (RegBaseCheckedException regBaseCheckedException) {
				LOGGER.error(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
						RegistrationConstants.APPLICATION_ID, regBaseCheckedException.getMessage());
			}
		}

	}

	private void setPreviewContent() {
		saveBiometricDetailsBtn.setVisible(false);
		biometricPrevBtn.setVisible(false);
		nextBtn.setVisible(false);
		pane2NextBtn.setVisible(false);
		pane2PrevBtn.setVisible(false);
		autoFillBtn.setVisible(false);
		fetchBtn.setVisible(false);
		SessionContext.getInstance().getMapObject().put("demoGraphicPane1Content", demoGraphicPane1);
		SessionContext.getInstance().getMapObject().put("demoGraphicPane2Content", demoGraphicPane2);
	}

	private boolean validateApplicantImage() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "validating applicant biometrics");

		boolean imageCaptured = false;
		if (applicantImageCaptured) {
			if (toggleBiometricException) {
				if (exceptionImageCaptured) {
					if (getRegistrationDtoContent() != null
							&& getRegistrationDtoContent().getDemographicDTO() != null) {
						imageCaptured = true;
					} else {
						generateAlert(RegistrationConstants.ALERT_ERROR,
								RegistrationConstants.DEMOGRAPHIC_DETAILS_ERROR_CONTEXT);
					}
				} else {
					generateAlert(RegistrationConstants.ALERT_ERROR, RegistrationConstants.APPLICANT_IMAGE_ERROR);
				}
			} else {
				if (getRegistrationDtoContent() != null && getRegistrationDtoContent().getDemographicDTO() != null) {
					imageCaptured = true;
				} else {
					generateAlert(RegistrationConstants.ALERT_ERROR,
							RegistrationConstants.DEMOGRAPHIC_DETAILS_ERROR_CONTEXT);
				}
			}
		} else {
			generateAlert(RegistrationConstants.ALERT_ERROR, RegistrationConstants.APPLICANT_IMAGE_ERROR);
		}
		return imageCaptured;
	}

	private void loadScreen(String screen) throws IOException {
		Parent createRoot = BaseController.load(RegistrationController.class.getResource(screen),
				applicationContext.getApplicationLanguageBundle());
		getScene(createRoot);
	}

	/**
	 * Validating the age field for the child/Infant check.
	 */
	public void ageValidationInDatePicker() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Validating the age given by DatePiker");

			if (ageDatePicker.getValue() != null) {
				LocalDate selectedDate = ageDatePicker.getValue();
				Date date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
				long ageInMilliSeconds = new Date().getTime() - date.getTime();
				long ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMilliSeconds);
				int age = (int) ageInDays / 365;
				if (age < Integer.parseInt(AppConfig.getApplicationProperty("age_limit_for_child"))) {
					childSpecificFields.setVisible(true);
					isChild = true;
					// documentFields.setLayoutY(134.00);
				} else {
					isChild = false;
					childSpecificFields.setVisible(false);
					// documentFields.setLayoutY(25.00);
				}
				// to populate age based on date of birth
				ageField.setText("" + (Period.between(ageDatePicker.getValue(), LocalDate.now()).getYears()));
			}
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Validated the age given by DatePiker");
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - VALIDATION OF AGE FOR DATEPICKER FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * Disabling the future days in the date picker calendar.
	 */
	private void disableFutureDays() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Disabling future dates");

			ageDatePicker.setDayCellFactory(picker -> new DateCell() {
				@Override
				public void updateItem(LocalDate date, boolean empty) {
					super.updateItem(date, empty);
					LocalDate today = LocalDate.now();

					setDisable(empty || date.compareTo(today) > 0);
				}
			});

			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Future dates disabled");
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - DISABLE FUTURE DATE FAILED", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * Populating the user language fields to local language fields
	 */
	private void populateTheLocalLangFields() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Populating the local language fields");
			fullName.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					if (!validation.validateTextField(fullName, fullName.getId() + "_ontype")) {
						fullName.setText(oldValue);
					} else {
						fullNameLocalLanguage.setText(fullName.getText());
					}
				}
			});

			addressLine1.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					if (!validation.validateTextField(addressLine1, addressLine1.getId() + "_ontype")) {
						addressLine1.setText(oldValue);
					} else {
						addressLine1LocalLanguage.setText(addressLine1.getText());
					}
				}
			});

			addressLine2.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					addressLine2LocalLanguage.setText(addressLine2.getText());
				}
			});

			addressLine3.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					addressLine3LocalLanguage.setText(addressLine3.getText());
				}
			});

			mobileNo.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					if (!validation.validateTextField(mobileNo, mobileNo.getId() + "_ontype")) {
						mobileNo.setText(oldValue);
					}
				}
			});

			emailId.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					if (!validation.validateTextField(emailId, emailId.getId() + "_ontype")) {
						emailId.setText(oldValue);
					}
				}
			});

			cniOrPinNumber.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					if (!validation.validateTextField(cniOrPinNumber, cniOrPinNumber.getId() + "_ontype")) {
						cniOrPinNumber.setText(oldValue);
					}
				}
			});

			parentName.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					if (!validation.validateTextField(parentName, parentName.getId() + "_ontype")) {
						parentName.setText(oldValue);
					}
				}
			});

			uinId.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					if (!validation.validateTextField(uinId, uinId.getId() + "_ontype")) {
						uinId.setText(oldValue);
					}
				}
			});

			postalCode.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(final ObservableValue<? extends String> obsVal, final String oldValue,
						final String newValue) {
					if (!validation.validateTextField(postalCode, postalCode.getId() + "_ontype")) {
						postalCode.setText(oldValue);
					}
				}
			});

			copyAddressImage.setOnMouseEntered((e) -> {
				copyAddressLabel.setVisible(true);
			});

			copyAddressImage.setOnMouseExited((e) -> {
				copyAddressLabel.setVisible(false);
			});

		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - LOCAL FIELD POPULATION FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * To restrict the user not to enter any values other than integer values.
	 */
	private void loadLanguageSpecificKeyboard() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Loading the local language keyboard");
			addressLine1LocalLanguage.focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

					if (oldValue) {
						keyboardNode.setVisible(false);
					}

				}
			});

			addressLine2LocalLanguage.focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

					if (oldValue) {
						keyboardNode.setVisible(false);
					}

				}
			});

			addressLine3LocalLanguage.focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

					if (oldValue) {
						keyboardNode.setVisible(false);
					}

				}
			});

			fullNameLocalLanguage.focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

					if (oldValue) {
						keyboardNode.setVisible(false);
					}

				}
			});
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - KEYBOARD LOADING FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * To restrict the user not to enter any values other than integer values.
	 */
	private void ageFieldValidations() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Validating the age given by age field");
			ageField.textProperty().addListener((obsValue, oldValue, newValue) -> {
				if (!validation.validateTextField(ageField, ageField.getId() + "_ontype")) {
					ageField.setText(oldValue);
				}
				int age = 0;
				if (newValue.matches("\\d{1,3}")) {
					if (Integer.parseInt(ageField.getText()) > Integer
							.parseInt(AppConfig.getApplicationProperty("max_age"))) {
						ageField.setText(oldValue);
						generateAlert(RegistrationConstants.ALERT_ERROR, RegistrationConstants.MAX_AGE_WARNING + " "
								+ AppConfig.getApplicationProperty("max_age"));
					} else {
						age = Integer.parseInt(ageField.getText());
						LocalDate currentYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
						LocalDate dob = currentYear.minusYears(age);
						autoAgeDatePicker.setValue(dob);
						if (age < Integer.parseInt(AppConfig.getApplicationProperty("age_limit_for_child"))) {
							childSpecificFields.setVisible(true);
							isChild = true;
							documentScanController.documentScan.setLayoutY(134.00);
						} else {
							isChild = false;
							childSpecificFields.setVisible(false);
							documentScanController.documentScan.setLayoutY(25.00);
						}
					}
				}

			});
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Validating the age given by age field");
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - AGE FIELD VALIDATION FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * Toggle functionality between age field and date picker.
	 */
	private void toggleFunction() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					"Entering into toggle function for toggle label 1 and toggle level 2");

			toggleLabel1.setId("toggleLabel1");
			toggleLabel2.setId("toggleLabel2");
			switchedOn.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
					if (newValue) {
						toggleLabel1.setId("toggleLabel2");
						toggleLabel2.setId("toggleLabel1");
						ageField.clear();
						ageDatePicker.setValue(null);
						parentName.clear();
						uinId.clear();
						childSpecificFields.setVisible(false);
						ageDatePicker.setDisable(true);
						ageField.setDisable(false);
						toggleAgeOrDobField = true;

					} else {
						toggleLabel1.setId("toggleLabel1");
						toggleLabel2.setId("toggleLabel2");
						ageField.clear();
						ageDatePicker.setValue(null);
						parentName.clear();
						uinId.clear();
						childSpecificFields.setVisible(false);
						ageDatePicker.setDisable(false);
						ageField.setDisable(true);
						toggleAgeOrDobField = false;

					}
				}
			});

			toggleLabel1.setOnMouseClicked((event) -> {
				switchedOn.set(!switchedOn.get());
			});
			toggleLabel2.setOnMouseClicked((event) -> {
				switchedOn.set(!switchedOn.get());
			});
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					"Exiting the toggle function for toggle label 1 and toggle level 2");
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - TOGGLING OF DOB AND AGE FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * To dispaly the selected date in the date picker in specific
	 * format("dd-mm-yyyy").
	 */
	private void dateFormatter() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Validating the date format");

			ageDatePicker.setConverter(new StringConverter<LocalDate>() {
				String pattern = "dd-MM-yyyy";
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

				{
					ageDatePicker.setPromptText(pattern.toLowerCase());
				}

				@Override
				public String toString(LocalDate date) {
					return date != null ? dateFormatter.format(date) : "";

				}

				@Override
				public LocalDate fromString(String string) {
					if (string != null && !string.isEmpty()) {
						return LocalDate.parse(string, dateFormatter);
					} else {
						return null;
					}
				}
			});
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - DATE FORMAT VALIDATION FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	/**
	 * 
	 * Opens the home page screen
	 * 
	 */
	@Override
	public void goToHomePage() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Going to home page");

		try {
			SessionContext.getInstance().getMapObject().remove(RegistrationConstants.REGISTRATION_ISEDIT);
			SessionContext.getInstance().getMapObject().remove(RegistrationConstants.REGISTRATION_PANE1_DATA);
			SessionContext.getInstance().getMapObject().remove(RegistrationConstants.REGISTRATION_PANE2_DATA);
			SessionContext.getInstance().getMapObject().remove(RegistrationConstants.REGISTRATION_AGE_DATA);
			SessionContext.getInstance().getMapObject().remove(RegistrationConstants.REGISTRATION_DATA);
			SessionContext.getInstance().getUserContext().getUserMap()
					.remove(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION);
			SessionContext.getInstance().getMapObject().remove(RegistrationConstants.DUPLICATE_FINGER);
			BaseController.load(getClass().getResource(RegistrationConstants.HOME_PAGE));
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - REGSITRATION_HOME_PAGE_LAYOUT_LOADING_FAILED", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, ioException.getMessage());
		}
	}

	/**
	 * 
	 * Validates the fields of demographic pane1
	 * 
	 */
	private boolean validateDemographicPaneOne() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Validating the fields in first demographic pane");

		boolean gotoNext = true;
		List<String> excludedIds = new ArrayList<String>();
		excludedIds.add("preRegistrationId");
		excludedIds.add("region");
		excludedIds.add("city");
		excludedIds.add("province");
		excludedIds.add("localAdminAuthority");
		excludedIds.add("virtualKeyboard");
		validation.setChild(isChild);
		gotoNext = validation.validateTheFields(demoGraphicPane1, excludedIds, gotoNext);
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Validated the fields");
		return gotoNext;
	}

	/**
	 * 
	 * Validate the fields of demographic pane 2
	 * 
	 */

	private boolean validateDemographicPaneTwo() {

		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Validating the fields in second demographic pane");
		boolean gotoNext = true;
		List<String> excludedIds = new ArrayList<String>();
		gotoNext = validation.validateTheFields(demoGraphicPane2, excludedIds, gotoNext);

		return gotoNext;
	}

	/**
	 * 
	 * Loading the the labels of local language fields
	 * 
	 */
	private void loadLocalLanguageFields() throws IOException {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Loading label fields of local language");
			ResourceBundle localProperties = applicationContext.getLocalLanguageProperty();
			fullNameLocalLanguageLabel.setText(localProperties.getString("full_name"));
			addressLine1LocalLanguagelabel.setText(localProperties.getString("address_line1"));
			addressLine2LocalLanguagelabel.setText(localProperties.getString("address_line2"));
			addressLine3LocalLanguagelabel.setText(localProperties.getString("address_line3"));
			String userlangTitle = demoGraphicTitlePane.getText();
			demoGraphicTitlePane.expandedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

					if (oldValue) {
						demoGraphicTitlePane.setText(userlangTitle);
					}

					if (newValue) {
						demoGraphicTitlePane.setText("    " + userlangTitle
								+ "                                                              " + ApplicationContext
										.getInstance().getLocalLanguageProperty().getString("titleDemographicPane"));

					}
				}
			});
		} catch (RuntimeException exception) {
			LOGGER.error("REGISTRATION - LOADING LOCAL LANGUAGE FIELDS FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, exception.getMessage());
		}
	}

	public RegistrationDTO getRegistrationDtoContent() {
		return (RegistrationDTO) SessionContext.getInstance().getMapObject()
				.get(RegistrationConstants.REGISTRATION_DATA);
	}

	private DatePicker getAgeDatePickerContent() {
		return (DatePicker) SessionContext.getInstance().getMapObject()
				.get(RegistrationConstants.REGISTRATION_AGE_DATA);
	}

	private Boolean isEditPage() {
		if (SessionContext.getInstance().getMapObject().get(RegistrationConstants.REGISTRATION_ISEDIT) != null)
			return (Boolean) SessionContext.getInstance().getMapObject().get(RegistrationConstants.REGISTRATION_ISEDIT);
		return false;
	}

	public void clickMe() {
		fullName.setText("Taleev Aalam");
		int age = 45;
		ageField.setText("" + age);
		toggleAgeOrDobField = true;
		gender.setValue("MALE");
		addressLine1.setText("Mind Tree Ltd");
		addressLine2.setText("RamanuJan It park");
		addressLine3.setText("Taramani");
		region.setText("Taramani");
		city.setText("Chennai");
		province.setText("Tamilnadu");
		postalCode.setText("60011");
		localAdminAuthority.setText("MindTree");
		mobileNo.setText("866769383");
		emailId.setText("taleev.aalam@mindtree.com");
		cniOrPinNumber.setText("012345678901234567890123456789");
		parentName.setText("Mokhtar");
		uinId.setText("93939939");
	}

	@FXML
	private void gotoFirstDemographicPane() {
		demoGraphicTitlePane.setContent(null);
		demoGraphicTitlePane.setExpanded(false);
		demoGraphicTitlePane.setContent(demoGraphicPane1);
		demoGraphicTitlePane.setExpanded(true);
		anchorPaneRegistration.setMaxHeight(900);
	}

	/**
	 * Toggle functionality for biometric exception
	 */
	private void toggleFunctionForBiometricException() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Entering into toggle function for Biometric exception");

			if (SessionContext.getInstance().getUserContext().getUserMap()
					.get(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION) == null) {
				toggleBiometricException = false;
				SessionContext.getInstance().getUserContext().getUserMap()
						.put(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION, toggleBiometricException);

			} else {
				toggleBiometricException = (boolean) SessionContext.getInstance().getUserContext().getUserMap()
						.get(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION);
			}

			if (toggleBiometricException) {
				bioExceptionToggleLabel1.setId("toggleLabel2");
				bioExceptionToggleLabel2.setId("toggleLabel1");
			} else {
				bioExceptionToggleLabel1.setId("toggleLabel1");
				bioExceptionToggleLabel2.setId("toggleLabel2");
			}

			switchedOnForBiometricException.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
					if (newValue) {
						bioExceptionToggleLabel1.setId("toggleLabel2");
						bioExceptionToggleLabel2.setId("toggleLabel1");
						toggleBiometricException = true;
						captureExceptionImage.setDisable(false);
					} else {
						bioExceptionToggleLabel1.setId("toggleLabel1");
						bioExceptionToggleLabel2.setId("toggleLabel2");
						toggleBiometricException = false;
						captureExceptionImage.setDisable(true);
					}
					SessionContext.getInstance().getUserContext().getUserMap()
							.put(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION, toggleBiometricException);
				}
			});
			bioExceptionToggleLabel1.setOnMouseClicked((event) -> {
				switchedOnForBiometricException.set(!switchedOnForBiometricException.get());
			});
			bioExceptionToggleLabel2.setOnMouseClicked((event) -> {
				switchedOnForBiometricException.set(!switchedOnForBiometricException.get());
			});
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Exiting the toggle function for Biometric exception");
		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - TOGGLING FOR BIOMETRIC EXCEPTION SWITCH FAILED ", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());
		}
	}

	public AnchorPane getBiometricsPane() {
		return biometricsPane;
	}

	public void setBiometricsPane(AnchorPane biometricsPane) {
		this.biometricsPane = biometricsPane;
	}

	/**
	 * @return the demoGraphicTitlePane
	 */
	public TitledPane getDemoGraphicTitlePane() {
		return demoGraphicTitlePane;
	}

	/**
	 * @param demoGraphicTitlePane
	 *            the demoGraphicTitlePane to set
	 */
	public void setDemoGraphicTitlePane(TitledPane demoGraphicTitlePane) {
		this.demoGraphicTitlePane = demoGraphicTitlePane;
	}

	// Operator Authentication
	public void goToAuthenticationPage() {
		try {
			SessionContext.getInstance().getMapObject().put(RegistrationConstants.REGISTRATION_ISEDIT, true);
			loadScreen(RegistrationConstants.CREATE_PACKET_PAGE);

			if (toggleBiometricException) {
				authenticationController.initData(ProcessNames.EXCEPTION.getType());
			} else {
				authenticationController.initData(ProcessNames.PACKET.getType());

			}
			accord.setExpandedPane(authenticationTitlePane);
			headerImage.setImage(new Image(RegistrationConstants.OPERATOR_AUTHENTICATION_LOGO));

			biometricTitlePane.setDisable(true);
			demoGraphicTitlePane.setDisable(true);
			authenticationTitlePane.setDisable(false);
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - REGSITRATION_OPERATOR_AUTHENTICATION_PAGE_LOADING_FAILED", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, ioException.getMessage());
		}
	}

	/**
	 * This method toggles the visible property of the PhotoCapture Pane.
	 * 
	 * @param visibility
	 *            the value of the visible property to be set
	 */
	public void togglePhotoCaptureVisibility(boolean visibility) {
		if (visibility) {
			if (capturePhotoUsingDevice.equals("Y")) {
				getBiometricsPane().setVisible(true);
			} else if (capturePhotoUsingDevice.equals("N")) {
				saveBiometricDetails();
				getBiometricsPane().setVisible(false);
			}
		} else {
			getBiometricsPane().setVisible(visibility);
		}
	}

	private void createRegistrationDTOObject() {
		RegistrationDTO registrationDTO = new RegistrationDTO();

		// Set the RID
		registrationDTO.setRegistrationId(RIDGenerator.nextRID());

		// Create objects for Biometric DTOS
		BiometricDTO biometricDTO = new BiometricDTO();
		biometricDTO.setApplicantBiometricDTO(createBiometricInfoDTO());
		biometricDTO.setIntroducerBiometricDTO(createBiometricInfoDTO());
		biometricDTO.setOperatorBiometricDTO(createBiometricInfoDTO());
		biometricDTO.setSupervisorBiometricDTO(createBiometricInfoDTO());
		registrationDTO.setBiometricDTO(biometricDTO);

		// Create object for Demographic DTOS
		DemographicDTO demographicDTO = new DemographicDTO();
		ApplicantDocumentDTO applicantDocumentDTO = new ApplicantDocumentDTO();
		applicantDocumentDTO.setDocumentDetailsDTO(new ArrayList<>());
		demographicDTO.setApplicantDocumentDTO(applicantDocumentDTO);
		DemographicInfoDTO demographicInfoDTOUser = new DemographicInfoDTO();
		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setLocationDTO(new LocationDTO());
		demographicInfoDTOUser.setAddressDTO(addressDTO);

		DemographicInfoDTO demographicInfoDTOLocal = new DemographicInfoDTO();
		AddressDTO addressDTOLocal = new AddressDTO();
		addressDTO.setLocationDTO(new LocationDTO());
		demographicInfoDTOLocal.setAddressDTO(addressDTOLocal);

		demographicDTO.setDemoInLocalLang(demographicInfoDTOLocal);
		demographicDTO.setDemoInUserLang(demographicInfoDTOUser);
		registrationDTO.setDemographicDTO(demographicDTO);

		// Create object for OSIData DTO
		registrationDTO.setOsiDataDTO(new OSIDataDTO());

		// Create object for RegistrationMetaData DTO
		RegistrationMetaDataDTO registrationMetaDataDTO = new RegistrationMetaDataDTO();
		registrationMetaDataDTO.setRegistrationCategory("New");
		registrationDTO.setRegistrationMetaDataDTO(registrationMetaDataDTO);

		// Put the RegistrationDTO object to SessionContext Map
		SessionContext.getInstance().getMapObject().put(RegistrationConstants.REGISTRATION_DATA, registrationDTO);
	}

	private BiometricInfoDTO createBiometricInfoDTO() {
		BiometricInfoDTO biometricInfoDTO = new BiometricInfoDTO();
		biometricInfoDTO.setFingerPrintBiometricExceptionDTO(new ArrayList<>());
		biometricInfoDTO.setFingerprintDetailsDTO(new ArrayList<>());
		biometricInfoDTO.setIrisBiometricExceptionDTO(new ArrayList<>());
		biometricInfoDTO.setIrisDetailsDTO(new ArrayList<>());
		return biometricInfoDTO;
	}

	/**
	 * This method toggles the visible property of the IrisCapture Pane.
	 * 
	 * @param visibility
	 *            the value of the visible property to be set
	 */
	public void toggleIrisCaptureVisibility(boolean visibility) {
		this.irisCapture.setVisible(visibility);
	}

	/**
	 * This method toggles the visible property of the FingerprintCapture Pane.
	 * 
	 * @param visibility
	 *            the value of the visible property to be set
	 */
	public void toggleFingerprintCaptureVisibility(boolean visibility) {
		this.fingerPrintCapturePane.setVisible(visibility);
	}

}
