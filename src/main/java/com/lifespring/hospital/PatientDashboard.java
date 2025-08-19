package com.lifespring.hospital;

import java.util.ArrayList;
import java.util.stream.Collectors;
import com.lifespring.hospital.dao.DoctorDAO;
import com.lifespring.hospital.model.Doctor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.lifespring.hospital.model.Patient;
import com.lifespring.hospital.dao.PatientDAO;
import com.lifespring.hospital.database.DatabaseConnection;
import com.lifespring.hospital.model.User;
import com.lifespring.hospital.ui.LifeSpringLoginPanelFX;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PatientDashboard extends Application {

    private BorderPane mainLayout;
    private VBox sidebar;
    private VBox mainContent;
    private Patient currentPatient;
    private Stage primaryStage;

    // Database integration
    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;
    private List<Doctor> allDoctors;
    private List<String> departments;

    // Form fields for updates (we'll keep references)
    private TextField nameField;
    private TextField ageField;
    private RadioButton maleRadio;
    private RadioButton femaleRadio;
    private TextField phoneField;
    private TextField addressField;
    private ComboBox<String> bloodGroupCombo;
    private TextField heightField;
    private TextField weightField;
    private TextField emailField;

    // Navigation buttons
    private Button dashboardBtn;
    private Button updateInfoBtn;
    private Button appointmentsBtn;
    private Button reportsBtn;
    private Button medicalHistoryBtn;
    private Button viewInvoiceBtn;
    private Button helpDeskBtn;

    public PatientDashboard() {
        // Initialize database connections
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
        loadDoctorsAndDepartments();

        System.out.println("🏥 LifeSpring Hospital - Patient Dashboard Initializing");
        System.out.println("📅 Time: " + getCurrentBangladeshTime());
        System.out.println("👤 System Admin: sabbir-hasnat");
        System.out.println("💾 Database: Connected via PatientDAO and DoctorDAO");
    }

    public PatientDashboard(Patient patient) {
        this();
        this.currentPatient = patient;
    }

    public PatientDashboard(String username) {
        this();
        loadPatientFromDatabase(username);
    }

    public PatientDashboard(int patientId) {
        this();
        loadPatientFromDatabaseById(patientId);
    }

    /**
     * ENHANCED: Static run method with validation
     */
    public static void run(Stage stage, User user) {
        try {
            System.out.println("🚀 Starting Patient Dashboard via run method");
            System.out.println("👤 User: " + user.getFullName() + " (" + user.getRole() + ")");
            System.out.println("📅 Launch Time: " + getCurrentTimeStatic());

            PatientDashboard dashboard = new PatientDashboard(user.getUsername());

            if (dashboard.currentPatient == null) {
                System.err.println("❌ Patient dashboard launch failed - no patient data");
                return;
            }

            dashboard.start(stage);
            System.out.println("✅ Patient Dashboard launched successfully");

        } catch (Exception e) {
            System.err.println("❌ Error launching Patient Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ENHANCED: Static show method with validation
     */
    public static void show(Stage parentStage, User user) {
        try {
            System.out.println("🏥 Showing Patient Dashboard for: " + user.getFullName());

            PatientDashboard dashboard = new PatientDashboard(user.getUsername());

            if (dashboard.currentPatient == null) {
                System.err.println("❌ Patient dashboard show failed - no patient data");
                return;
            }

            Stage stage = new Stage();
            stage.setTitle("LifeSpring Hospital - Patient Portal - " + user.getFullName());

            dashboard.start(stage);

            if (parentStage != null) {
                parentStage.close();
            }

            System.out.println("✅ Patient Dashboard displayed successfully");

        } catch (Exception e) {
            System.err.println("❌ Error showing Patient Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load doctors and departments from database ONLY - no fallback data
     */
    private void loadDoctorsAndDepartments() {
        try {
            System.out.println("🔍 Loading doctors and departments from database...");

            // Load all active doctors from database ONLY
            this.allDoctors = doctorDAO.getAllDoctors();

            if (allDoctors != null && !allDoctors.isEmpty()) {
                // Extract unique specializations from actual database data
                this.departments = allDoctors.stream()
                        .map(Doctor::getSpecialization)
                        .filter(spec -> spec != null && !spec.trim().isEmpty())
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

                System.out.println("✅ Loaded " + allDoctors.size() + " doctors from " + departments.size() + " departments from database");

                // Print loaded departments for debugging
                System.out.println("📋 Available departments: " + departments);

            } else {
                System.err.println("❌ No doctors found in database");
                this.departments = new ArrayList<>();
                this.allDoctors = new ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading doctors/departments: " + e.getMessage());
            e.printStackTrace();

            // Initialize empty lists if database error occurs
            this.departments = new ArrayList<>();
            this.allDoctors = new ArrayList<>();
        }
    }

    /**
     * Get doctors by specialization from database data only
     */
    private List<Doctor> getDoctorsBySpecialization(String specialization) {
        if (specialization == null || allDoctors == null) {
            return new ArrayList<>();
        }

        List<Doctor> filteredDoctors = allDoctors.stream()
                .filter(doctor -> specialization.equals(doctor.getSpecialization()))
                .collect(Collectors.toList());

        System.out.println("🔍 Found " + filteredDoctors.size() + " doctors in " + specialization + " department");
        return filteredDoctors;
    }

    /**
     * ENHANCED: Load patient with detailed debugging
     */
    private void loadPatientFromDatabase(String username) {
        try {
            System.out.println("🔍 Loading patient from database: '" + username + "'");

            // Strategy 1: Exact username match
            this.currentPatient = patientDAO.getPatientByUsername(username);
            if (currentPatient != null) {
                System.out.println("✅ Found by exact username: " + currentPatient.getFullName());
                return;
            }

            // Strategy 2: Case-insensitive search
            this.currentPatient = patientDAO.getPatientByUsernameIgnoreCase(username);
            if (currentPatient != null) {
                System.out.println("✅ Found by case-insensitive search: " + currentPatient.getFullName());
                return;
            }

            // Strategy 3: Search by email if username looks like email
            if (username.contains("@")) {
                this.currentPatient = patientDAO.getPatientByEmail(username);
                if (currentPatient != null) {
                    System.out.println("✅ Found by email: " + currentPatient.getFullName());
                    return;
                }
            }

            System.err.println("❌ Patient not found in database: '" + username + "'");
            showPatientNotFoundErrorWithSuggestions(username);

        } catch (Exception e) {
            System.err.println("❌ Error loading patient from database: " + e.getMessage());
            e.printStackTrace();
            showDatabaseError(e.getMessage());
        }
    }

    private void loadPatientFromDatabaseById(int patientId) {
        try {
            System.out.println("🔍 Loading patient from database by ID: " + patientId);
            this.currentPatient = patientDAO.getPatientById(patientId);

            if (currentPatient != null) {
                System.out.println("✅ Patient loaded successfully: " + currentPatient.getFullName());
            } else {
                System.err.println("❌ Patient not found with ID: " + patientId);
                showPatientNotFoundError("ID: " + patientId);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading patient by ID: " + e.getMessage());
            e.printStackTrace();
            showDatabaseError(e.getMessage());
        }
    }

    private void showPatientNotFoundErrorWithSuggestions(String username) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Patient Not Found");
            alert.setHeaderText("Patient Record Not Found");

            StringBuilder message = new StringBuilder();
            message.append("No patient record found for username: '").append(username).append("'\n\n");
            message.append("Possible solutions:\n");
            message.append("1. Check username spelling and case sensitivity\n");
            message.append("2. Try using your email address to login\n");
            message.append("3. Contact system administrator to verify your patient record\n");
            message.append("4. Ensure your patient record has a username set\n\n");
            message.append("You will be redirected to login screen.");

            alert.setContentText(message.toString());
            alert.showAndWait();

            returnToLoginScreen();
        });
    }

    private void showPatientNotFoundError(String identifier) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Patient Not Found");
            alert.setHeaderText("Patient Record Not Found");
            alert.setContentText("No patient record found for: " + identifier +
                    "\n\nPlease contact system administrator to create your patient record." +
                    "\n\nYou will be redirected to login screen.");

            alert.showAndWait();
            returnToLoginScreen();
        });
    }

    private void showDatabaseError(String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Database Connection Failed");
            alert.setContentText("Unable to connect to patient database:\n\n" + errorMessage +
                    "\n\nPlease contact system administrator." +
                    "\n\nYou will be redirected to login screen.");

            alert.showAndWait();
            returnToLoginScreen();
        });
    }

    private void returnToLoginScreen() {
        try {
            System.out.println("🔄 Returning to login screen due to patient data issue");

            LifeSpringLoginPanelFX loginPanel = new LifeSpringLoginPanelFX();
            Stage loginStage = new Stage();
            loginPanel.start(loginStage);

            if (primaryStage != null) {
                primaryStage.close();
            }

            System.out.println("✅ Redirected to login screen");

        } catch (Exception e) {
            System.err.println("❌ Error returning to login screen: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        if (currentPatient == null) {
            System.err.println("❌ No patient data available for dashboard");
            showPatientNotFoundError("Unknown");
            return;
        }

        System.out.println("👥 Current Patient: " + currentPatient.getFullName());
        System.out.println("🆔 Patient ID: " + currentPatient.getId());

        createMainLayout();
        showDashboard();

        // FIXED: Set exact window size as requested (950x700)
        Scene scene = new Scene(mainLayout, 950, 700);

        primaryStage.setTitle("LifeSpring Hospital - Patient Portal - " + currentPatient.getFullName());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Fixed size as requested
        primaryStage.centerOnScreen();
        primaryStage.show();

        System.out.println("✅ Patient Dashboard launched successfully!");
    }

    /**
     * ENHANCED: Create main layout with header elements in right panel
     */
    private void createMainLayout() {
        mainLayout = new BorderPane();

        // Create smaller sidebar
        VBox sidebar = createStyledSidebar();
        sidebar.setPrefWidth(220); // Reduced width
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(220);

        // Create top box for right header elements only
        HBox topBox = createTopBox();

        // Create main content area with header elements
        mainContent = new VBox();
        mainContent.setPadding(new Insets(15, 20, 20, 20));
        mainContent.setSpacing(15);
        mainContent.setStyle("-fx-background-color: #f8f9fa;");

        // Create right panel with top box and main content
        VBox rightPanel = new VBox();
        rightPanel.setSpacing(0);
        rightPanel.getChildren().addAll(topBox, mainContent);

        // Set layout components
        mainLayout.setLeft(sidebar);         // Smaller sidebar
        mainLayout.setCenter(rightPanel);    // Top box + main content with header elements
    }

    /**
     * ENHANCED: Create top box with more spacing (moved down)
     */
    private HBox createTopBox() {
        HBox topBox = new HBox();
        topBox.setPrefHeight(55); // Increased height to move down
        topBox.setMinHeight(55);
        topBox.setMaxHeight(55);
        topBox.setPadding(new Insets(15, 20, 15, 20)); // More padding to move down
        topBox.setAlignment(Pos.CENTER_RIGHT);
        topBox.setSpacing(15);

        // Top box styling - clean and minimal
        topBox.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 0 0 1 0;"
        );

        // Database status
        HBox dbStatusBox = new HBox();
        dbStatusBox.setAlignment(Pos.CENTER);
        dbStatusBox.setSpacing(6);

        Label dbIcon = new Label("🟢");
        dbIcon.setStyle("-fx-font-size: 12px;");

        Label dbStatusLabel = new Label("Database Connected");
        dbStatusLabel.setStyle(
                "-fx-text-fill: #27ae60; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold;"
        );

        dbStatusBox.getChildren().addAll(dbIcon, dbStatusLabel);

        // Logout button
        Button logoutButton = new Button("Log Out");
        logoutButton.setStyle(
                "-fx-background-color: #FF6B47; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 8 18 8 18; " +
                        "-fx-border-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        // Hover effects
        logoutButton.setOnMouseEntered(e -> {
            logoutButton.setStyle(
                    "-fx-background-color: #E55A42; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 8 18 8 18; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);"
            );
        });

        logoutButton.setOnMouseExited(e -> {
            logoutButton.setStyle(
                    "-fx-background-color: #FF6B47; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 20; " +
                            "-fx-padding: 8 18 8 18; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            );
        });

        logoutButton.setOnAction(e -> handleLogout());

        topBox.getChildren().addAll(dbStatusBox, logoutButton);
        return topBox;
    }

    /**
     * FIXED: Create larger sidebar text with better spacing
     */
    private VBox createStyledSidebar() {
        sidebar = new VBox();
        sidebar.setPrefWidth(220); // Reduced width
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(220);
        sidebar.setSpacing(0);
        sidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);"
        );

        // Hospital header section - minimal padding for higher position
        VBox headerSection = new VBox();
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(15, 15, 20, 15)); // Reduced padding
        headerSection.setSpacing(8); // Increased spacing between elements

        Label logoIcon = new Label("🏥");
        logoIcon.setStyle(
                "-fx-font-size: 40px; " + // BIGGER icon
                        "-fx-text-fill: white;"
        );

        Label hospitalName = new Label("LifeSpring Hospital");
        hospitalName.setStyle(
                "-fx-font-size: 18px; " + // BIGGER font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white; " +
                        "-fx-text-alignment: center;"
        );

        Label portalLabel = new Label("Patient Portal");
        portalLabel.setStyle(
                "-fx-font-size: 13px; " + // BIGGER font
                        "-fx-text-fill: #ecf0f1; " +
                        "-fx-text-alignment: center;"
        );

        headerSection.getChildren().addAll(logoIcon, hospitalName, portalLabel);

        // Navigation buttons section
        VBox navSection = new VBox();
        navSection.setSpacing(8); // Increased spacing
        navSection.setPadding(new Insets(10, 15, 15, 15)); // More padding

        // Create BIGGER navigation buttons
        dashboardBtn = createStyledNavButton("📊", "Dashboard", true);
        updateInfoBtn = createStyledNavButton("📝","Update Information", false);
        appointmentsBtn = createStyledNavButton("📅", "Appointments", false);
        reportsBtn = createStyledNavButton("📄", "Reports", false);
        medicalHistoryBtn = createStyledNavButton("🏥", "Medical History", false);
        viewInvoiceBtn = createStyledNavButton("💰", "View Invoice", false);

        // Setup navigation actions
        setupNavigationActions();

        navSection.getChildren().addAll(
                dashboardBtn, updateInfoBtn, appointmentsBtn,
                reportsBtn, medicalHistoryBtn, viewInvoiceBtn
        );

        // Help desk button at bottom
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        helpDeskBtn = createStyledNavButton("🆘", "Help Desk", false);
        helpDeskBtn.setOnAction(e -> {
            setActiveButton(helpDeskBtn);
            showHelpDesk();
        });

        VBox helpSection = new VBox();
        helpSection.setPadding(new Insets(0, 15, 15, 15));
        helpSection.getChildren().add(helpDeskBtn);

        sidebar.getChildren().addAll(headerSection, navSection, bottomSpacer, helpSection);
        return sidebar;
    }

    /**
     * FIXED: Create bigger styled navigation button with proper spacing
     */
    private Button createStyledNavButton(String icon, String text, boolean active) {
        Button button = new Button();
        button.setPrefWidth(190); // Same width
        button.setPrefHeight(45); // BIGGER height

        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(8); // FIXED: Reduced gap between emoji and text
        content.setPadding(new Insets(0, 0, 0, 15)); // More left padding

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;"); // BIGGER icon

        Label textLabel = new Label(text);
        textLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " + // BIGGER font
                        "-fx-font-weight: " + (active ? "bold" : "normal") + ";"
        );

        content.getChildren().addAll(iconLabel, textLabel);
        button.setGraphic(content);

        if (active) {
            button.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.25); " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            );
        } else {
            button.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand;"
            );
        }

        // Hover effects
        button.setOnMouseEntered(e -> {
            if (!button.getStyle().contains("rgba(255, 255, 255, 0.25)")) {
                button.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.15); " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: transparent; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
                );
            }
        });

        button.setOnMouseExited(e -> {
            if (!button.getStyle().contains("rgba(255, 255, 255, 0.25)")) {
                button.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: transparent; " +
                                "-fx-cursor: hand;"
                );
            }
        });

        return button;
    }

    /**
     * ENHANCED: Setup navigation button actions
     */
    private void setupNavigationActions() {
        dashboardBtn.setOnAction(e -> {
            setActiveButton(dashboardBtn);
            showDashboard();
        });

        updateInfoBtn.setOnAction(e -> {
            setActiveButton(updateInfoBtn);
            showUpdateInformation();
        });

        appointmentsBtn.setOnAction(e -> {
            setActiveButton(appointmentsBtn);
            showAppointments();
        });

        reportsBtn.setOnAction(e -> {
            setActiveButton(reportsBtn);
            showReports();
        });

        medicalHistoryBtn.setOnAction(e -> {
            setActiveButton(medicalHistoryBtn);
            showMedicalHistory();
        });

        viewInvoiceBtn.setOnAction(e -> {
            setActiveButton(viewInvoiceBtn);
            showViewInvoice();
        });
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {dashboardBtn, updateInfoBtn, appointmentsBtn, reportsBtn, medicalHistoryBtn, viewInvoiceBtn, helpDeskBtn};

        for (Button button : buttons) {
            // Reset to inactive style
            button.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand;"
            );

            // Update text style to normal weight
            HBox content = (HBox) button.getGraphic();
            if (content != null && content.getChildren().size() > 1) {
                Label textLabel = (Label) content.getChildren().get(1);
                textLabel.setStyle(
                        "-fx-text-fill: white; " +
                                "-fx-font-size: 14px; " + // BIGGER font
                                "-fx-font-weight: normal;"
                );
            }
        }

        // Set active style
        activeButton.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.25); " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        // Update text style to bold
        HBox content = (HBox) activeButton.getGraphic();
        if (content != null && content.getChildren().size() > 1) {
            Label textLabel = (Label) content.getChildren().get(1);
            textLabel.setStyle(
                    "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " + // BIGGER font
                            "-fx-font-weight: bold;"
            );
        }
    }

    /**
     * ENHANCED: Show dashboard without app title and moved up with larger cards
     */
    private void showDashboard() {
        mainContent.getChildren().clear();

        System.out.println("📊 Loading dashboard with database data for: " + currentPatient.getFullName());

        // Welcome section (removed app title as requested)
        VBox welcomeSection = new VBox();
        welcomeSection.setSpacing(5);
        welcomeSection.setPadding(new Insets(0, 0, 8, 0)); // Reduced bottom padding to move up

        Label titleLabel = new Label("Hello, " + currentPatient.getFullName() + "!");
        titleLabel.setStyle(
                "-fx-font-size: 26px; " + // Slightly larger
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        Label subtitleLabel = new Label("Your health is our priority!");
        subtitleLabel.setStyle(
                "-fx-font-size: 15px; " + // Slightly larger
                        "-fx-text-fill: #7f8c8d;"
        );

        Label dbInfoLabel = new Label("📊 Real patient data from database - Last updated: " + getCurrentBangladeshTime());
        dbInfoLabel.setStyle(
                "-fx-font-size: 10px; " +
                        "-fx-text-fill: #27ae60; " +
                        "-fx-padding: 5 0 0 0;"
        );

        welcomeSection.getChildren().addAll(titleLabel, subtitleLabel, dbInfoLabel);

        // Main content area with larger cards and moved up
        HBox mainGrid = new HBox();
        mainGrid.setSpacing(18); // Increased spacing
        mainGrid.setAlignment(Pos.TOP_LEFT);

        // LEFT COLUMN (2 cards vertically) - larger cards
        VBox leftColumn = new VBox();
        leftColumn.setSpacing(15); // Increased spacing
        leftColumn.setPrefWidth(350); // Wider

        VBox patientProfileCard = createPatientProfileCard();
        patientProfileCard.setPrefWidth(350);
        patientProfileCard.setPrefHeight(180); // Increased height

        VBox patientInfoCard = createPatientInformationCard();
        patientInfoCard.setPrefWidth(350);
        patientInfoCard.setPrefHeight(250); // Increased height

        leftColumn.getChildren().addAll(patientProfileCard, patientInfoCard);

        // RIGHT COLUMN (3 cards vertically) - larger cards
        VBox rightColumn = new VBox();
        rightColumn.setSpacing(15); // Increased spacing
        rightColumn.setPrefWidth(400); // Wider

        // Top: Health stats (3 small cards in a row)
        HBox healthStatsRow = createHealthStatsRow();
        healthStatsRow.setPrefHeight(110); // Increased height

        // Middle: Test Reports card
        VBox testReportsCard = createTestReportsCard();
        testReportsCard.setPrefWidth(400);
        testReportsCard.setPrefHeight(160); // Increased height

        // Bottom: Upcoming Appointment card
        VBox upcomingAppointmentCard = createUpcomingAppointmentCard();
        upcomingAppointmentCard.setPrefWidth(400);
        upcomingAppointmentCard.setPrefHeight(145); // Increased height

        rightColumn.getChildren().addAll(healthStatsRow, testReportsCard, upcomingAppointmentCard);

        // Add columns to main grid
        mainGrid.getChildren().addAll(leftColumn, rightColumn);

        // Add everything to main content - NO APP TITLE
        mainContent.getChildren().addAll(welcomeSection, mainGrid);
    }

    /**
     * Create patient profile card (top left) - larger size
     */
    private VBox createPatientProfileCard() {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(12); // Increased spacing
        card.setPadding(new Insets(20)); // Increased padding
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);"
        );

        Label profileIcon = new Label("👤");
        profileIcon.setStyle(
                "-fx-font-size: 55px; " + // Larger icon
                        "-fx-text-fill: #bdc3c7;"
        );

        Label nameLabel = new Label(currentPatient.getFullName() != null ? currentPatient.getFullName() : "Unknown Patient");
        nameLabel.setStyle(
                "-fx-font-size: 16px; " + // Larger font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        Label ageLabel = new Label("Age: " + currentPatient.getAge());
        ageLabel.setStyle(
                "-fx-font-size: 14px; " + // Larger font
                        "-fx-text-fill: #7f8c8d;"
        );

        Button updateButton = new Button("Update");
        updateButton.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " + // Larger font
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 8 25 8 25; " + // More padding
                        "-fx-border-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        updateButton.setOnAction(e -> {
            setActiveButton(updateInfoBtn);
            showUpdateInformation();
        });

        card.getChildren().addAll(profileIcon, nameLabel, ageLabel, updateButton);
        return card;
    }

    /**
     * Create patient information card (bottom left) - larger size
     */
    private VBox createPatientInformationCard() {
        VBox card = new VBox();
        card.setSpacing(12); // Increased spacing
        card.setPadding(new Insets(20)); // Increased padding
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);"
        );

        Label titleLabel = new Label("Patient Information:");
        titleLabel.setStyle(
                "-fx-font-size: 16px; " + // Larger font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #34495e;"
        );

        VBox infoList = new VBox();
        infoList.setSpacing(8); // Increased spacing

        String infoStyle = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;"; // Larger font

        Label genderLabel = new Label("Gender: " + (currentPatient.getGender() != null ? currentPatient.getGender() : "Not specified"));
        genderLabel.setStyle(infoStyle);

        Label bloodTypeLabel = new Label("Blood Type: " + (currentPatient.getBloodGroup() != null ? currentPatient.getBloodGroup() : "Not specified"));
        bloodTypeLabel.setStyle(infoStyle);

        Label diseaseLabel = new Label("Disease: " + (currentPatient.getDisease() != null ? currentPatient.getDisease() : "None"));
        diseaseLabel.setStyle(infoStyle);

        Label heightLabel = new Label("Height: " + (currentPatient.getHeight() != null ? currentPatient.getHeight() : "Not specified"));
        heightLabel.setStyle(infoStyle);

        Label weightLabel = new Label("Weight: " + (currentPatient.getWeight() != null ? currentPatient.getWeight() : "Not specified"));
        weightLabel.setStyle(infoStyle);

        Label patientIdLabel = new Label("Patient ID: " + currentPatient.getId());
        patientIdLabel.setStyle(infoStyle);

        infoList.getChildren().addAll(
                genderLabel, bloodTypeLabel, diseaseLabel,
                heightLabel, weightLabel, patientIdLabel
        );

        card.getChildren().addAll(titleLabel, infoList);
        return card;
    }

    /**
     * Create health stats row (top right - 3 small cards) - larger size
     */
    private HBox createHealthStatsRow() {
        HBox row = new HBox();
        row.setSpacing(15); // Increased spacing
        row.setAlignment(Pos.CENTER_LEFT);

        VBox heartRateCard = createHealthStatCard("❤", "Heart Rate", "80 bpm", "#e74c3c");
        VBox temperatureCard = createHealthStatCard("🌡", "Body Temperature", "36.5°c", "#1abc9c");
        VBox hemoglobinCard = createHealthStatCard("📈", "Hemoglobin", "16.4 g/dl", "#3498db");

        // Set larger widths
        heartRateCard.setPrefWidth(125); // Increased width
        temperatureCard.setPrefWidth(125);
        hemoglobinCard.setPrefWidth(125);

        row.getChildren().addAll(heartRateCard, temperatureCard, hemoglobinCard);
        return row;
    }

    /**
     * Create individual health stat card - larger size
     */
    private VBox createHealthStatCard(String icon, String title, String value, String accentColor) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(6); // Increased spacing
        card.setPadding(new Insets(12)); // Increased padding
        card.setPrefHeight(110); // Increased height
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3);"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;"); // Larger icon

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 10px; " + // Slightly larger
                        "-fx-text-fill: #7f8c8d; " +
                        "-fx-text-alignment: center;"
        );
        titleLabel.setWrapText(true);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 14px; " + // Larger font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + accentColor + "; " +
                        "-fx-text-alignment: center;"
        );

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    /**
     * Create test reports card (middle right) - larger size
     */
    private VBox createTestReportsCard() {
        VBox card = new VBox();
        card.setSpacing(12); // Increased spacing
        card.setPadding(new Insets(18)); // Increased padding
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);"
        );

        Label titleLabel = new Label("Test Reports:");
        titleLabel.setStyle(
                "-fx-font-size: 17px; " + // Larger font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        HBox reportsRow = new HBox();
        reportsRow.setSpacing(18); // Increased spacing
        reportsRow.setAlignment(Pos.CENTER_LEFT);

        VBox ctScanReport = createTestReportItem("CT Scan-Full Body", "12 July, 2025");
        VBox xrayReport = createTestReportItem("X-Ray", "18 July, 2025");

        ctScanReport.setPrefWidth(185); // Increased width
        xrayReport.setPrefWidth(185);

        reportsRow.getChildren().addAll(ctScanReport, xrayReport);

        card.getChildren().addAll(titleLabel, reportsRow);
        return card;
    }

    /**
     * Create individual test report item - larger size
     */
    private VBox createTestReportItem(String title, String date) {
        VBox item = new VBox();
        item.setAlignment(Pos.CENTER);
        item.setSpacing(5); // Increased spacing
        item.setPadding(new Insets(14)); // Increased padding
        item.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 13px; " + // Larger font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-text-alignment: center;"
        );
        titleLabel.setWrapText(true);

        Label dateLabel = new Label(date);
        dateLabel.setStyle(
                "-fx-font-size: 11px; " + // Larger font
                        "-fx-text-fill: #7f8c8d; " +
                        "-fx-text-alignment: center;"
        );

        item.getChildren().addAll(titleLabel, dateLabel);
        return item;
    }

    /**
     * Create upcoming appointment card (bottom right) - larger size
     */
    private VBox createUpcomingAppointmentCard() {
        VBox card = new VBox();
        card.setSpacing(10); // Increased spacing
        card.setPadding(new Insets(18)); // Increased padding
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);"
        );

        Label titleLabel = new Label("Upcoming appointment:");
        titleLabel.setStyle(
                "-fx-font-size: 17px; " + // Larger font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        Label doctorLabel = new Label("[Doctor name]");
        doctorLabel.setStyle(
                "-fx-font-size: 14px; " + // Larger font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #34495e;"
        );

        VBox leftDetails = new VBox();
        leftDetails.setSpacing(4); // Increased spacing

        Label departmentLabel = new Label("Department:");
        Label roomLabel = new Label("Room no:");

        String detailStyle = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;"; // Larger font

        departmentLabel.setStyle(detailStyle);
        roomLabel.setStyle(detailStyle);

        leftDetails.getChildren().addAll(departmentLabel, roomLabel);

        // Date and time section
        HBox appointmentDateTime = new HBox();
        appointmentDateTime.setSpacing(30); // Increased spacing
        appointmentDateTime.setAlignment(Pos.CENTER_LEFT);
        appointmentDateTime.setPadding(new Insets(10)); // Increased padding
        appointmentDateTime.setStyle(
                "-fx-background-color: #ecf0f1; " +
                        "-fx-background-radius: 8;"
        );

        Label dateText = new Label("10th September, 2025");
        dateText.setStyle(
                "-fx-font-size: 12px; " + // Larger font
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-font-weight: bold;"
        );

        Label timeText = new Label("8:00 PM");
        timeText.setStyle(
                "-fx-font-size: 12px; " + // Larger font
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-font-weight: bold;"
        );

        appointmentDateTime.getChildren().addAll(dateText, timeText);

        card.getChildren().addAll(titleLabel, doctorLabel, leftDetails, appointmentDateTime);
        return card;
    }

    // Continue with the rest of the methods...
    // I'll add the remaining methods in the next part due to length constraints

    /**
     * FIXED: Show MORE COMPACT update information form that fits better
     */
    private void showUpdateInformation() {
        mainContent.getChildren().clear();

        System.out.println("✏️ Loading Update Information form for: " + currentPatient.getFullName());

        // Initialize form fields with current patient data
        nameField = createVeryCompactTextField(currentPatient.getFullName());
        ageField = createVeryCompactTextField(String.valueOf(currentPatient.getAge()));
        phoneField = createVeryCompactTextField(currentPatient.getPhone());
        addressField = createVeryCompactTextField(currentPatient.getAddress());
        heightField = createVeryCompactTextField(currentPatient.getHeight());
        weightField = createVeryCompactTextField(currentPatient.getWeight());
        emailField = createVeryCompactTextField(currentPatient.getEmail());

        // VERY COMPACT form container
        VBox formContainer = new VBox();
        formContainer.setSpacing(10); // Reduced spacing
        formContainer.setPadding(new Insets(5, 0, 10, 0)); // Reduced padding

        // COMPACT form title
        Label titleLabel = new Label("Update Patient Information");
        titleLabel.setStyle(
                "-fx-font-size: 20px; " + // Smaller title
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        // VERY COMPACT form card
        VBox formCard = new VBox();
        formCard.setMaxWidth(650); // Smaller width
        formCard.setPadding(new Insets(20, 25, 20, 25)); // Reduced padding
        formCard.setSpacing(12); // Reduced spacing
        formCard.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " + // Smaller radius
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " + // Smaller shadow
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12;"
        );

        // Create VERY COMPACT form fields in 2 columns
        HBox row1 = new HBox();
        row1.setSpacing(15); // Reduced spacing
        VBox nameCol = createVeryCompactFormField("Name:", nameField);
        VBox ageCol = createVeryCompactFormField("Age:", ageField);
        nameCol.setPrefWidth(280); // Smaller width
        ageCol.setPrefWidth(120); // Smaller width
        row1.getChildren().addAll(nameCol, ageCol);

        HBox row2 = new HBox();
        row2.setSpacing(15);
        VBox genderCol = createVeryCompactGenderField();
        VBox bloodGroupCol = createVeryCompactBloodGroupField();
        genderCol.setPrefWidth(200); // Smaller width
        bloodGroupCol.setPrefWidth(200);
        row2.getChildren().addAll(genderCol, bloodGroupCol);

        VBox phoneRow = createVeryCompactFormField("Phone Number:", phoneField);
        VBox addressRow = createVeryCompactFormField("Address:", addressField);

        HBox row3 = new HBox();
        row3.setSpacing(15);
        VBox heightCol = createVeryCompactFormField("Height:", heightField);
        VBox weightCol = createVeryCompactFormField("Weight:", weightField);
        heightCol.setPrefWidth(200);
        weightCol.setPrefWidth(200);
        row3.getChildren().addAll(heightCol, weightCol);

        VBox emailRow = createVeryCompactFormField("Email:", emailField);

        // Add all form fields to card
        formCard.getChildren().addAll(
                row1, row2, phoneRow, addressRow, row3, emailRow
        );

        // VERY COMPACT button container
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(12); // Reduced spacing
        buttonContainer.setPadding(new Insets(10, 0, 0, 0)); // Reduced padding

        // COMPACT buttons
        Button saveButton = createVeryCompactButton("Save", "#28a745", "#218838");
        saveButton.setOnAction(e -> handleSavePatientInfoEnhanced());

        Button cancelButton = createVeryCompactButton("Cancel", "#6c757d", "#545b62");
        cancelButton.setOnAction(e -> {
            setActiveButton(dashboardBtn);
            showDashboard();
        });

        buttonContainer.getChildren().addAll(saveButton, cancelButton);

        // Add everything to form container
        formContainer.getChildren().addAll(titleLabel, formCard, buttonContainer);

        // Add to main content
        mainContent.getChildren().add(formContainer);
    }

    /**
     * Create VERY COMPACT text field
     */
    private TextField createVeryCompactTextField(String text) {
        TextField textField = new TextField();
        if (text != null) {
            textField.setText(text);
        }
        textField.setPrefHeight(30); // Smaller height
        textField.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 5; " + // Smaller radius
                        "-fx-background-radius: 5; " +
                        "-fx-font-size: 12px; " + // Smaller font
                        "-fx-text-fill: #495057; " +
                        "-fx-padding: 6 10 6 10;" // Smaller padding
        );

        // Focus effects
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                textField.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-border-color: #007bff; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-radius: 5; " +
                                "-fx-background-radius: 5; " +
                                "-fx-font-size: 12px; " +
                                "-fx-text-fill: #495057; " +
                                "-fx-padding: 6 10 6 10;"
                );
            } else {
                textField.setStyle(
                        "-fx-background-color: #f8f9fa; " +
                                "-fx-border-color: #ced4da; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 5; " +
                                "-fx-background-radius: 5; " +
                                "-fx-font-size: 12px; " +
                                "-fx-text-fill: #495057; " +
                                "-fx-padding: 6 10 6 10;"
                );
            }
        });

        return textField;
    }

    /**
     * Create VERY COMPACT form field
     */
    private VBox createVeryCompactFormField(String labelText, TextField textField) {
        VBox fieldContainer = new VBox();
        fieldContainer.setSpacing(3); // Smaller spacing

        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-size: 12px; " + // Smaller font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        fieldContainer.getChildren().addAll(label, textField);
        return fieldContainer;
    }

    /**
     * Create VERY COMPACT gender selection field
     */
    private VBox createVeryCompactGenderField() {
        VBox genderContainer = new VBox();
        genderContainer.setSpacing(3); // Smaller spacing

        Label genderLabel = new Label("Gender:");
        genderLabel.setStyle(
                "-fx-font-size: 12px; " + // Smaller font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        HBox radioContainer = new HBox();
        radioContainer.setSpacing(12); // Smaller spacing

        ToggleGroup genderGroup = new ToggleGroup();

        maleRadio = new RadioButton("Male");
        maleRadio.setToggleGroup(genderGroup);
        maleRadio.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;"); // Smaller font

        femaleRadio = new RadioButton("Female");
        femaleRadio.setToggleGroup(genderGroup);
        femaleRadio.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;"); // Smaller font

        // Set current gender
        if (currentPatient.getGender() != null) {
            if (currentPatient.getGender().equalsIgnoreCase("Male")) {
                maleRadio.setSelected(true);
            } else if (currentPatient.getGender().equalsIgnoreCase("Female")) {
                femaleRadio.setSelected(true);
            }
        }

        radioContainer.getChildren().addAll(maleRadio, femaleRadio);
        genderContainer.getChildren().addAll(genderLabel, radioContainer);

        return genderContainer;
    }

    /**
     * Create VERY COMPACT blood group selection field
     */
    private VBox createVeryCompactBloodGroupField() {
        VBox bloodGroupContainer = new VBox();
        bloodGroupContainer.setSpacing(3); // Smaller spacing

        Label bloodGroupLabel = new Label("Blood Group:");
        bloodGroupLabel.setStyle(
                "-fx-font-size: 12px; " + // Smaller font
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        bloodGroupCombo = new ComboBox<>();
        bloodGroupCombo.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodGroupCombo.setPrefHeight(30); // Smaller height
        bloodGroupCombo.setPrefWidth(160); // Smaller width
        bloodGroupCombo.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-font-size: 12px; " + // Smaller font
                        "-fx-text-fill: #495057;"
        );

        // Set current blood group
        if (currentPatient.getBloodGroup() != null) {
            bloodGroupCombo.setValue(currentPatient.getBloodGroup());
        }

        bloodGroupContainer.getChildren().addAll(bloodGroupLabel, bloodGroupCombo);
        return bloodGroupContainer;
    }

    /**
     * Create VERY COMPACT styled button
     */
    private Button createVeryCompactButton(String text, String normalColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefWidth(90); // Smaller width
        button.setPrefHeight(30); // Smaller height
        button.setStyle(
                "-fx-background-color: " + normalColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " + // Smaller font
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " + // Smaller radius
                        "-fx-border-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);" // Smaller shadow
        );

        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: " + hoverColor + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 6; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 4, 0, 0, 2);"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + normalColor + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 6; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);"
            );
        });

        return button;
    }

    /**
     * Enhanced save method using direct field references
     */
    private void handleSavePatientInfoEnhanced() {
        try {
            System.out.println("💾 Saving patient information to database...");
            System.out.println("👤 Patient ID: " + currentPatient.getId());

            // Get values from form fields directly
            String updatedName = nameField.getText();
            String updatedAgeText = ageField.getText();
            String updatedPhone = phoneField.getText();
            String updatedAddress = addressField.getText();
            String updatedHeight = heightField.getText();
            String updatedWeight = weightField.getText();
            String updatedEmail = emailField.getText();

            // Get gender from radio buttons
            String updatedGender = null;
            if (maleRadio.isSelected()) {
                updatedGender = "Male";
            } else if (femaleRadio.isSelected()) {
                updatedGender = "Female";
            }

            // Get blood group from combo box
            String updatedBloodGroup = bloodGroupCombo.getValue();

            // Validate required fields
            if (updatedName == null || updatedName.trim().isEmpty()) {
                showValidationError("Name is required!");
                nameField.requestFocus();
                return;
            }

            if (updatedAgeText == null || updatedAgeText.trim().isEmpty()) {
                showValidationError("Age is required!");
                ageField.requestFocus();
                return;
            }

            // Validate age is numeric
            int age;
            try {
                age = Integer.parseInt(updatedAgeText.trim());
                if (age < 0 || age > 150) {
                    showValidationError("Please enter a valid age (0-150)!");
                    ageField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showValidationError("Age must be a valid number!");
                ageField.requestFocus();
                return;
            }

            // Validate email format if provided
            if (updatedEmail != null && !updatedEmail.trim().isEmpty()) {
                if (!isValidEmail(updatedEmail.trim())) {
                    showValidationError("Please enter a valid email address!");
                    emailField.requestFocus();
                    return;
                }
            }

            // Update current patient object
            currentPatient.setFullName(updatedName.trim());
            currentPatient.setAge(age);
            currentPatient.setGender(updatedGender);
            currentPatient.setPhone(updatedPhone != null ? updatedPhone.trim() : null);
            currentPatient.setAddress(updatedAddress != null ? updatedAddress.trim() : null);
            currentPatient.setBloodGroup(updatedBloodGroup);
            currentPatient.setHeight(updatedHeight != null ? updatedHeight.trim() : null);
            currentPatient.setWeight(updatedWeight != null ? updatedWeight.trim() : null);
            currentPatient.setEmail(updatedEmail != null ? updatedEmail.trim() : null);

            // Save to database using PatientDAO
            boolean updateSuccess = patientDAO.updatePatient(currentPatient);

            if (updateSuccess) {
                System.out.println("✅ Patient information updated successfully in database");
                System.out.println("📊 Updated Patient: " + currentPatient.getFullName());
                System.out.println("🆔 Patient ID: " + currentPatient.getId());
                System.out.println("📅 Update Time: " + getCurrentBangladeshTime());

                // Show success message
                showSuccessMessage(
                        "Patient information has been updated successfully!\n\n" +
                                "✅ Name: " + currentPatient.getFullName() + "\n" +
                                "✅ Age: " + currentPatient.getAge() + "\n" +
                                "✅ Gender: " + (currentPatient.getGender() != null ? currentPatient.getGender() : "Not specified") + "\n" +
                                "✅ Blood Group: " + (currentPatient.getBloodGroup() != null ? currentPatient.getBloodGroup() : "Not specified") + "\n" +
                                "✅ Phone: " + (currentPatient.getPhone() != null ? currentPatient.getPhone() : "Not specified")
                );

                // Return to dashboard with updated data
                setActiveButton(dashboardBtn);
                showDashboard();

            } else {
                System.err.println("❌ Failed to update patient information in database");
                showDatabaseError("Failed to update patient information in database.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error saving patient information: " + e.getMessage());
            e.printStackTrace();
            showDatabaseError("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    /**
     * Create styled button helper method
     */
    private Button createStyledButton(String text, String normalColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefWidth(120);
        button.setPrefHeight(45);
        button.setStyle(
                "-fx-background-color: " + normalColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );

        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: " + hoverColor + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 10; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + normalColor + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 10; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );
        });

        return button;
    }

    /**
     * Show validation error message
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);

        // Style the alert
        alert.getDialogPane().setStyle(
                "-fx-background-color: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 13px;"
        );

        alert.showAndWait();
    }

    /**
     * Show success message
     */
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Update Successful");
        alert.setContentText(message);

        // Style the alert
        alert.getDialogPane().setStyle(
                "-fx-background-color: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 13px;"
        );

        alert.showAndWait();
    }

    // ========================================
    // PLACEHOLDER METHODS FOR OTHER MENU ITEMS
    // ========================================

    /**
     * ENHANCED: Show appointments page with booking functionality
     */
    private void showAppointments() {
        mainContent.getChildren().clear();

        System.out.println("📅 Loading Appointments page for: " + currentPatient.getFullName());

        // Main container
        VBox appointmentsContainer = new VBox();
        appointmentsContainer.setSpacing(20);
        appointmentsContainer.setPadding(new Insets(10, 0, 20, 0));

        // Page title
        Label titleLabel = new Label("Appointments");
        titleLabel.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-alignment: center;"
        );

        // Action buttons row
        HBox actionButtons = new HBox();
        actionButtons.setSpacing(20);
        actionButtons.setAlignment(Pos.CENTER);

        Button bookAppointmentBtn = new Button("Book Appointment");
        bookAppointmentBtn.setStyle(
                "-fx-background-color: #87CEEB; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 30 12 30; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        Button myAppointmentsBtn = new Button("My Appointments");
        myAppointmentsBtn.setStyle(
                "-fx-background-color: #87CEEB; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 30 12 30; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        actionButtons.getChildren().addAll(bookAppointmentBtn, myAppointmentsBtn);

        // Main content area
        HBox mainContentArea = new HBox();
        mainContentArea.setSpacing(25);
        mainContentArea.setAlignment(Pos.TOP_LEFT);

        // LEFT: Booking form
        VBox bookingForm = createBookingForm();
        bookingForm.setPrefWidth(400);

        // RIGHT: Appointment details
        VBox appointmentDetails = createAppointmentDetails();
        appointmentDetails.setPrefWidth(350);

        mainContentArea.getChildren().addAll(bookingForm, appointmentDetails);

        // Add everything to main container
        appointmentsContainer.getChildren().addAll(titleLabel, actionButtons, mainContentArea);
        mainContent.getChildren().add(appointmentsContainer);
    }

    /**
     * Create ENHANCED booking form with DATABASE DATA ONLY
     */
    private VBox createBookingForm() {
        VBox form = new VBox();
        form.setSpacing(10);
        form.setPadding(new Insets(15));
        form.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15;"
        );

        // Dynamic Calendar section with current month
        LocalDate currentDate = LocalDate.now();
        String currentMonth = currentDate.format(DateTimeFormatter.ofPattern("MMMM"));

        Label calendarTitle = new Label(currentMonth);
        calendarTitle.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-alignment: center;"
        );

        // Dynamic Date buttons (next 7 days)
        HBox dateRow = new HBox();
        dateRow.setSpacing(6);
        dateRow.setAlignment(Pos.CENTER);

        List<LocalDate> next7Days = getNext7Days();
        Button[] dateButtons = new Button[7];

        for (int i = 0; i < 7; i++) {
            LocalDate date = next7Days.get(i);
            dateButtons[i] = new Button(String.valueOf(date.getDayOfMonth()));
            dateButtons[i].setPrefSize(35, 35);

            // Set today as selected by default
            if (i == 0) {
                dateButtons[i].setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 12px; " +
                                "-fx-background-radius: 6; " +
                                "-fx-cursor: hand;"
                );
            } else {
                dateButtons[i].setStyle(
                        "-fx-background-color: #f8f9fa; " +
                                "-fx-text-fill: #2c3e50; " +
                                "-fx-font-size: 12px; " +
                                "-fx-background-radius: 6; " +
                                "-fx-border-color: #dee2e6; " +
                                "-fx-border-width: 1; " +
                                "-fx-cursor: hand;"
                );
            }

            // Add click handler for date selection
            final int index = i;
            dateButtons[i].setOnAction(e -> {
                // Reset all buttons
                for (Button btn : dateButtons) {
                    btn.setStyle(
                            "-fx-background-color: #f8f9fa; " +
                                    "-fx-text-fill: #2c3e50; " +
                                    "-fx-font-size: 12px; " +
                                    "-fx-background-radius: 6; " +
                                    "-fx-border-color: #dee2e6; " +
                                    "-fx-border-width: 1; " +
                                    "-fx-cursor: hand;"
                    );
                }

                // Set selected button
                dateButtons[index].setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 12px; " +
                                "-fx-background-radius: 6; " +
                                "-fx-cursor: hand;"
                );
            });

            dateRow.getChildren().add(dateButtons[i]);
        }

        // Available time
        Label timeLabel = new Label("Available time:");
        timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
                "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM");
        timeCombo.setValue("10:00 AM");
        timeCombo.setPrefWidth(180);
        timeCombo.setPrefHeight(28);
        timeCombo.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-radius: 5; " +
                        "-fx-font-size: 12px;"
        );

        // Department - loaded from DATABASE ONLY
        Label deptLabel = new Label("Department:");
        deptLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ComboBox<String> deptCombo = new ComboBox<>();
        deptCombo.setPrefWidth(180);
        deptCombo.setPrefHeight(28);
        deptCombo.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-radius: 5; " +
                        "-fx-font-size: 12px;"
        );

        // Only load departments if we have data from database
        if (departments != null && !departments.isEmpty()) {
            deptCombo.getItems().addAll(departments);
            deptCombo.setValue(departments.get(0)); // Set first department as default
            System.out.println("📋 Loaded departments in ComboBox: " + departments);
        } else {
            // If no departments found, show appropriate message
            deptCombo.getItems().add("No departments available");
            deptCombo.setValue("No departments available");
            deptCombo.setDisable(true);
            System.err.println("❌ No departments available from database");
        }

        // Doctor - dynamically loaded based on department from DATABASE ONLY
        Label doctorLabel = new Label("Doctor:");
        doctorLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ComboBox<String> doctorCombo = new ComboBox<>();
        doctorCombo.setPrefWidth(180);
        doctorCombo.setPrefHeight(28);
        doctorCombo.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-radius: 5; " +
                        "-fx-font-size: 12px;"
        );

        // Load doctors for initial department (DATABASE ONLY)
        if (!departments.isEmpty()) {
            updateDoctorComboBoxFromDatabase(doctorCombo, deptCombo.getValue());
        } else {
            doctorCombo.getItems().add("No doctors available");
            doctorCombo.setValue("No doctors available");
            doctorCombo.setDisable(true);
        }

        // Add department change listener (DATABASE ONLY)
        deptCombo.setOnAction(e -> {
            String selectedDepartment = deptCombo.getValue();
            if (selectedDepartment != null && !selectedDepartment.equals("No departments available")) {
                updateDoctorComboBoxFromDatabase(doctorCombo, selectedDepartment);
                System.out.println("🏥 Department changed to: " + selectedDepartment);
            }
        });

        // Patient details
        Label patientLabel = new Label("Appointment for...");
        patientLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Name field
        VBox nameContainer = new VBox();
        nameContainer.setSpacing(3);

        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");

        TextField nameField = new TextField(currentPatient.getFullName());
        nameField.setPrefWidth(180);
        nameField.setPrefHeight(25);
        nameField.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-radius: 4; " +
                        "-fx-font-size: 11px; " +
                        "-fx-padding: 4 8 4 8;"
        );

        nameContainer.getChildren().addAll(nameLabel, nameField);

        // Phone field
        VBox phoneContainer = new VBox();
        phoneContainer.setSpacing(3);

        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");

        TextField phoneField = new TextField(currentPatient.getPhone() != null ? currentPatient.getPhone() : "017............");
        phoneField.setPrefWidth(180);
        phoneField.setPrefHeight(25);
        phoneField.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-radius: 4; " +
                        "-fx-font-size: 11px; " +
                        "-fx-padding: 4 8 4 8;"
        );

        phoneContainer.getChildren().addAll(phoneLabel, phoneField);

        // Action buttons
        HBox buttonRow = new HBox();
        buttonRow.setSpacing(8);
        buttonRow.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("Save");
        saveBtn.setPrefWidth(70);
        saveBtn.setPrefHeight(28);
        saveBtn.setStyle(
                "-fx-background-color: #28a745; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 11px; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 5 15 5 15; " +
                        "-fx-cursor: hand;"
        );

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(70);
        cancelBtn.setPrefHeight(28);
        cancelBtn.setStyle(
                "-fx-background-color: #dc3545; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 11px; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 5 15 5 15; " +
                        "-fx-cursor: hand;"
        );

        // Enhanced save action with database data validation
        saveBtn.setOnAction(e -> {
            // Check if valid data is selected
            if (deptCombo.getValue().equals("No departments available") ||
                    doctorCombo.getValue().equals("No doctors available")) {

                showValidationError("No doctors available in database!\n\n" +
                        "Please ensure doctors are added to the database before booking appointments.");
                return;
            }

            // Get selected date
            LocalDate selectedDate = null;
            for (int i = 0; i < dateButtons.length; i++) {
                if (dateButtons[i].getStyle().contains("#e74c3c")) {
                    selectedDate = next7Days.get(i);
                    break;
                }
            }

            String formattedDate = selectedDate != null ?
                    selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM, yyyy")) : "Unknown date";

            showSuccessMessage("Appointment booked successfully!\n\n" +
                    "📅 Date: " + formattedDate + "\n" +
                    "🕙 Time: " + timeCombo.getValue() + "\n" +
                    "🏥 Department: " + deptCombo.getValue() + "\n" +
                    "👨‍⚕️ Doctor: " + doctorCombo.getValue() + "\n" +
                    "👤 Patient: " + nameField.getText() + "\n" +
                    "📞 Phone: " + phoneField.getText() + "\n\n" +
                    "✅ Data loaded from database by: sabbir-hasnat");
        });

        cancelBtn.setOnAction(e -> {
            setActiveButton(dashboardBtn);
            showDashboard();
        });

        buttonRow.getChildren().addAll(saveBtn, cancelBtn);

        // Add everything to form
        form.getChildren().addAll(
                calendarTitle,
                dateRow,
                timeLabel,
                timeCombo,
                deptLabel,
                deptCombo,
                doctorLabel,
                doctorCombo,
                patientLabel,
                nameContainer,
                phoneContainer,
                buttonRow
        );

        return form;
    }

    /**
     * Update doctor combo box based on selected department - DATABASE DATA ONLY
     */
    private void updateDoctorComboBoxFromDatabase(ComboBox<String> doctorCombo, String department) {
        doctorCombo.getItems().clear();

        if (department != null && allDoctors != null && !department.equals("No departments available")) {
            List<Doctor> departmentDoctors = getDoctorsBySpecialization(department);

            if (!departmentDoctors.isEmpty()) {
                for (Doctor doctor : departmentDoctors) {
                    String doctorName = "Dr. " + doctor.getFullName();
                    doctorCombo.getItems().add(doctorName);
                }
                doctorCombo.setValue(doctorCombo.getItems().get(0)); // Set first doctor as default
                doctorCombo.setDisable(false);

                System.out.println("✅ Loaded " + departmentDoctors.size() + " doctors for " + department + " from database");
            } else {
                // No doctors found for this department in database
                doctorCombo.getItems().add("No doctors available in " + department);
                doctorCombo.setValue("No doctors available in " + department);
                doctorCombo.setDisable(true);

                System.out.println("⚠️ No doctors found in database for department: " + department);
            }
        } else {
            // Invalid department or no data
            doctorCombo.getItems().add("No doctors available");
            doctorCombo.setValue("No doctors available");
            doctorCombo.setDisable(true);

            System.err.println("❌ Invalid department or no database data available");
        }
    }

    /**
     * Create appointment details (right side)
     */
    private VBox createAppointmentDetails() {
        VBox details = new VBox();
        details.setSpacing(15);
        details.setPadding(new Insets(20));

        // Tab-like headers
        HBox tabHeader = new HBox();
        tabHeader.setSpacing(0);
        tabHeader.setAlignment(Pos.CENTER);

        Button upcomingTab = new Button("Upcoming");
        upcomingTab.setPrefWidth(120);
        upcomingTab.setStyle(
                "-fx-background-color: white; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-width: 0 0 2 0; " +
                        "-fx-background-radius: 0; " +
                        "-fx-cursor: hand;"
        );

        Button pastTab = new Button("Past");
        pastTab.setPrefWidth(120);
        pastTab.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-text-fill: #6c757d; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-width: 1 1 0 1; " +
                        "-fx-background-radius: 0; " +
                        "-fx-cursor: hand;"
        );

        tabHeader.getChildren().addAll(upcomingTab, pastTab);

        // Upcoming appointment details
        VBox upcomingDetails = new VBox();
        upcomingDetails.setSpacing(10);
        upcomingDetails.setPadding(new Insets(15));
        upcomingDetails.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        Label upcomingTitle = new Label("Upcoming appointment:");
        upcomingTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label doctorName = new Label("[Doctor name]");
        doctorName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #495057;");

        Label department = new Label("Department:");
        department.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        Label roomNo = new Label("Room no:");
        roomNo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        Label serialNo = new Label("Serial no:");
        serialNo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        // Date and time box
        HBox dateTimeBox = new HBox();
        dateTimeBox.setSpacing(20);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);
        dateTimeBox.setPadding(new Insets(10));
        dateTimeBox.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        HBox dateSection = new HBox();
        dateSection.setSpacing(5);
        dateSection.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("📅");
        Label dateText = new Label("10 th September,2025");
        dateText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        dateSection.getChildren().addAll(dateIcon, dateText);

        HBox timeSection = new HBox();
        timeSection.setSpacing(5);
        timeSection.setAlignment(Pos.CENTER_LEFT);
        Label timeIcon = new Label("🕰");
        Label timeText = new Label("8:00 pm");
        timeText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        timeSection.getChildren().addAll(timeIcon, timeText);

        dateTimeBox.getChildren().addAll(dateSection, timeSection);

        // Action buttons
        VBox actionButtons = new VBox();
        actionButtons.setSpacing(8);

        Button editBtn = new Button("Edit Appointment");
        editBtn.setPrefWidth(180);
        editBtn.setStyle(
                "-fx-background-color: white; " +
                        "-fx-text-fill: #495057; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        Button cancelBtn = new Button("Cancel Appointment");
        cancelBtn.setPrefWidth(180);
        cancelBtn.setStyle(
                "-fx-background-color: white; " +
                        "-fx-text-fill: #495057; " +
                        "-fx-border-color: #ced4da; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        actionButtons.getChildren().addAll(editBtn, cancelBtn);

        upcomingDetails.getChildren().addAll(
                upcomingTitle, doctorName, department, roomNo, serialNo,
                dateTimeBox, actionButtons
        );

        details.getChildren().addAll(tabHeader, upcomingDetails);
        return details;
    }

    // Continue with the remaining methods...

    /**
     * ENHANCED: Show reports page with test reports and prescriptions
     */
    private void showReports() {
        mainContent.getChildren().clear();

        System.out.println("📄 Loading Reports page for: " + currentPatient.getFullName());

        // Main container
        VBox reportsContainer = new VBox();
        reportsContainer.setSpacing(20);
        reportsContainer.setPadding(new Insets(10, 0, 20, 0));

        // Page title
        Label titleLabel = new Label("Reports");
        titleLabel.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-alignment: center;"
        );

        // Main content area
        HBox mainContentArea = new HBox();
        mainContentArea.setSpacing(25);
        mainContentArea.setAlignment(Pos.TOP_LEFT);

        // LEFT: Test Reports
        VBox testReportsSection = createTestReportsSection();
        testReportsSection.setPrefWidth(450);

        // RIGHT: Prescriptions
        VBox prescriptionsSection = createPrescriptionsSection();
        prescriptionsSection.setPrefWidth(450);

        mainContentArea.getChildren().addAll(testReportsSection, prescriptionsSection);

        // Add everything to main container
        reportsContainer.getChildren().addAll(titleLabel, mainContentArea);
        mainContent.getChildren().add(reportsContainer);
    }

    /**
     * Create test reports section (left side)
     */
    private VBox createTestReportsSection() {
        VBox section = new VBox();
        section.setSpacing(15);

        // Section header
        Button testReportsBtn = new Button("Test Reports");
        testReportsBtn.setPrefWidth(180);
        testReportsBtn.setStyle(
                "-fx-background-color: #87CEEB; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 20 12 20; " +
                        "-fx-cursor: hand;"
        );

        // Test reports container
        VBox reportsContainer = new VBox();
        reportsContainer.setSpacing(15);
        reportsContainer.setPadding(new Insets(25));
        reportsContainer.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15;"
        );

        // Title with icon
        HBox titleBox = new HBox();
        titleBox.setSpacing(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label reportIcon = new Label("🩺");
        reportIcon.setStyle("-fx-font-size: 18px;");

        Label reportsTitle = new Label("Test Reports:");
        reportsTitle.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        titleBox.getChildren().addAll(reportIcon, reportsTitle);

        // Report items
        VBox reportItems = new VBox();
        reportItems.setSpacing(12);

        // CT Scan report
        HBox ctScanReport = createReportItem("🔄", "CT Scan-Full Body", "12 July,2025");

        // X-Ray report
        HBox xrayReport = createReportItem("📱", "X-Ray", "18 July,2025");

        reportItems.getChildren().addAll(ctScanReport, xrayReport);

        reportsContainer.getChildren().addAll(titleBox, reportItems);
        section.getChildren().addAll(testReportsBtn, reportsContainer);

        return section;
    }

    /**
     * Create individual report item
     */
    private HBox createReportItem(String icon, String title, String date) {
        HBox item = new HBox();
        item.setSpacing(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15));
        item.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        VBox textBox = new VBox();
        textBox.setSpacing(2);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        Label dateLabel = new Label(date);
        dateLabel.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #6c757d;"
        );

        textBox.getChildren().addAll(titleLabel, dateLabel);

        // Add click handler
        item.setOnMouseClicked(e -> {
            showSuccessMessage("Opening " + title + " report...\n\n📄 Date: " + date + "\n📁 Status: Available for download");
        });

        // Hover effect
        item.setOnMouseEntered(e -> {
            item.setStyle(
                    "-fx-background-color: #e9ecef; " +
                            "-fx-background-radius: 12; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
        });

        item.setOnMouseExited(e -> {
            item.setStyle(
                    "-fx-background-color: #f8f9fa; " +
                            "-fx-background-radius: 12; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
            );
        });

        item.getChildren().addAll(iconLabel, textBox);
        return item;
    }

    /**
     * Create prescriptions section (right side)
     */
    private VBox createPrescriptionsSection() {
        VBox section = new VBox();
        section.setSpacing(15);

        // Section header
        Button prescriptionsBtn = new Button("Prescriptions");
        prescriptionsBtn.setPrefWidth(180);
        prescriptionsBtn.setStyle(
                "-fx-background-color: #87CEEB; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 20 12 20; " +
                        "-fx-cursor: hand;"
        );

        // Prescriptions container
        VBox prescriptionsContainer = new VBox();
        prescriptionsContainer.setSpacing(15);
        prescriptionsContainer.setPadding(new Insets(25));
        prescriptionsContainer.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15;"
        );

        // Action buttons
        VBox actionButtons = new VBox();
        actionButtons.setSpacing(12);
        actionButtons.setAlignment(Pos.CENTER);

        Button uploadBtn = new Button("Upload Prescription");
        uploadBtn.setPrefWidth(250);
        uploadBtn.setStyle(
                "-fx-background-color: #32CD32; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 20 12 20; " +
                        "-fx-cursor: hand;"
        );

        Button favoriteBtn = new Button("Add to Favorite");
        favoriteBtn.setPrefWidth(250);
        favoriteBtn.setStyle(
                "-fx-background-color: #4169E1; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 20 12 20; " +
                        "-fx-cursor: hand;"
        );

        Button myPrescriptionsBtn = new Button("My Prescriptions");
        myPrescriptionsBtn.setPrefWidth(250);
        myPrescriptionsBtn.setStyle(
                "-fx-background-color: #1E90FF; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 20 12 20; " +
                        "-fx-cursor: hand;"
        );

        // Add button actions
        uploadBtn.setOnAction(e -> {
            showSuccessMessage("Upload Prescription\n\n📄 Select prescription file to upload\n📁 Supported formats: PDF, JPG, PNG");
        });

        favoriteBtn.setOnAction(e -> {
            showSuccessMessage("Add to Favorites\n\n⭐ Mark important prescriptions as favorites\n📋 Quick access to frequently used prescriptions");
        });

        myPrescriptionsBtn.setOnAction(e -> {
            showSuccessMessage("My Prescriptions\n\n📋 View all your prescriptions\n📄 Download and manage prescription history");
        });

        actionButtons.getChildren().addAll(uploadBtn, favoriteBtn, myPrescriptionsBtn);
        prescriptionsContainer.getChildren().add(actionButtons);
        section.getChildren().addAll(prescriptionsBtn, prescriptionsContainer);

        return section;
    }

    /**
     * ENHANCED: Show medical history page with body diagram and stats
     */
    private void showMedicalHistory() {
        mainContent.getChildren().clear();

        System.out.println("🏥 Loading Medical History page for: " + currentPatient.getFullName());

        // Main container
        VBox historyContainer = new VBox();
        historyContainer.setSpacing(20);
        historyContainer.setPadding(new Insets(10, 0, 20, 0));

        // Page title
        Label titleLabel = new Label("Medical History");
        titleLabel.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-alignment: center;"
        );

        // Main content area
        HBox mainContentArea = new HBox();
        mainContentArea.setSpacing(30);
        mainContentArea.setAlignment(Pos.TOP_LEFT);

        // LEFT: Body diagram
        VBox bodyDiagramSection = createBodyDiagramSection();
        bodyDiagramSection.setPrefWidth(350);

        // RIGHT: Health stats
        VBox healthStatsSection = createMedicalStatsSection();
        healthStatsSection.setPrefWidth(400);

        mainContentArea.getChildren().addAll(bodyDiagramSection, healthStatsSection);

        // Add everything to main container
        historyContainer.getChildren().addAll(titleLabel, mainContentArea);
        mainContent.getChildren().add(historyContainer);
    }

    /**
     * Create body diagram section (left side)
     */
    private VBox createBodyDiagramSection() {
        VBox section = new VBox();
        section.setSpacing(20);
        section.setAlignment(Pos.CENTER);

        // Body silhouette (placeholder)
        VBox bodyContainer = new VBox();
        bodyContainer.setAlignment(Pos.CENTER);
        bodyContainer.setPrefHeight(400);
        bodyContainer.setStyle(
                "-fx-background-color: #87CEEB; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);"
        );

        // Simple body representation
        Label bodyIcon = new Label("🚶");
        bodyIcon.setStyle(
                "-fx-font-size: 120px; " +
                        "-fx-text-fill: rgba(255, 255, 255, 0.8);"
        );

        Label bodyLabel = new Label("Body Diagram");
        bodyLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        bodyContainer.getChildren().addAll(bodyIcon, bodyLabel);
        section.getChildren().add(bodyContainer);

        return section;
    }

    /**
     * Create medical stats section (right side)
     */
    private VBox createMedicalStatsSection() {
        VBox section = new VBox();
        section.setSpacing(20);

        // Stats grid
        VBox statsGrid = new VBox();
        statsGrid.setSpacing(15);

        // Row 1: Heart Rate and Body Temperature
        HBox row1 = new HBox();
        row1.setSpacing(15);
        row1.setAlignment(Pos.CENTER);

        VBox heartRateCard = createMedicalStatCard("❤", "Heart Rate", "80 bpm", "#e74c3c");
        VBox temperatureCard = createMedicalStatCard("🌡", "Body Temperature", "36.5°c", "#f39c12");

        heartRateCard.setPrefWidth(180);
        temperatureCard.setPrefWidth(180);

        row1.getChildren().addAll(heartRateCard, temperatureCard);

        // Row 2: Hemoglobin (centered)
        HBox row2 = new HBox();
        row2.setAlignment(Pos.CENTER);

        VBox hemoglobinCard = createMedicalStatCard("📈", "Hemoglobin", "16.4 g/dl", "#e74c3c");
        hemoglobinCard.setPrefWidth(180);

        row2.getChildren().add(hemoglobinCard);

        statsGrid.getChildren().addAll(row1, row2);
        section.getChildren().add(statsGrid);

        return section;
    }

    /**
     * Create medical stat card
     */
    private VBox createMedicalStatCard(String icon, String title, String value, String color) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.setPadding(new Insets(20));
        card.setPrefHeight(120);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 30px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-text-alignment: center;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + color + "; " +
                        "-fx-text-alignment: center;"
        );

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    /**
     * ENHANCED: Show invoice page with patient details and print functionality
     */
    private void showViewInvoice() {
        mainContent.getChildren().clear();

        System.out.println("💰 Loading View Invoice page for: " + currentPatient.getFullName());

        // Main container
        VBox invoiceContainer = new VBox();
        invoiceContainer.setSpacing(20);
        invoiceContainer.setPadding(new Insets(10, 0, 20, 0));

        // Page title
        Label titleLabel = new Label("Invoice");
        titleLabel.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-alignment: center;"
        );

        // Invoice form
        VBox invoiceForm = createInvoiceForm();
        invoiceForm.setMaxWidth(600);
        invoiceForm.setAlignment(Pos.CENTER);

        // Add everything to main container
        invoiceContainer.getChildren().addAll(titleLabel, invoiceForm);
        invoiceContainer.setAlignment(Pos.CENTER);
        mainContent.getChildren().add(invoiceContainer);
    }

    /**
     * Create invoice form
     */
    private VBox createInvoiceForm() {
        VBox form = new VBox();
        form.setSpacing(20);
        form.setPadding(new Insets(40));
        form.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4); " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15;"
        );

        // Patient information fields
        VBox fieldsContainer = new VBox();
        fieldsContainer.setSpacing(25);

        // Name field
        VBox nameField = createInvoiceField("Name:", currentPatient.getFullName());

        // Patient ID field
        VBox idField = createInvoiceField("Patient ID:", String.valueOf(currentPatient.getId()));

        // Additional spacing
        Region spacer = new Region();
        spacer.setPrefHeight(30);

        fieldsContainer.getChildren().addAll(nameField, idField, spacer);

        // Print button
        Button printButton = new Button("🖨 Print Invoice");
        printButton.setPrefWidth(200);
        printButton.setStyle(
                "-fx-background-color: #32CD32; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 12 25 12 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        // Print button action
        printButton.setOnAction(e -> {
            showSuccessMessage("Invoice Print Request\n\n" +
                    "📄 Patient: " + currentPatient.getFullName() + "\n" +
                    "🆔 Patient ID: " + currentPatient.getId() + "\n" +
                    "📅 Date: " + getCurrentBangladeshTime() + "\n\n" +
                    "🖨 Sending to printer...\n" +
                    "✅ Invoice will be printed shortly!");
        });

        // Hover effects for print button
        printButton.setOnMouseEntered(e -> {
            printButton.setStyle(
                    "-fx-background-color: #28a745; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 25; " +
                            "-fx-padding: 12 25 12 25; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);"
            );
        });

        printButton.setOnMouseExited(e -> {
            printButton.setStyle(
                    "-fx-background-color: #32CD32; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 25; " +
                            "-fx-padding: 12 25 12 25; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            );
        });

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().add(printButton);

        form.getChildren().addAll(fieldsContainer, buttonContainer);
        return form;
    }

    /**
     * Create invoice field
     */
    private VBox createInvoiceField(String labelText, String value) {
        VBox fieldContainer = new VBox();
        fieldContainer.setSpacing(8);

        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        // Underlined value area
        HBox valueContainer = new HBox();
        valueContainer.setAlignment(Pos.CENTER_LEFT);
        valueContainer.setSpacing(10);

        Label valueLabel = new Label(value != null ? value : "");
        valueLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #495057;"
        );

        // Underline
        Region underline = new Region();
        underline.setPrefHeight(2);
        underline.setPrefWidth(300);
        underline.setStyle("-fx-background-color: #2c3e50;");
        HBox.setHgrow(underline, Priority.ALWAYS);

        VBox valueSection = new VBox();
        valueSection.setSpacing(5);
        valueSection.getChildren().addAll(valueLabel, underline);

        fieldContainer.getChildren().addAll(label, valueSection);
        return fieldContainer;
    }

    /**
     * Show help desk page (placeholder)
     */
    private void showHelpDesk() {
        mainContent.getChildren().clear();

        System.out.println("🆘 Loading Help Desk page for: " + currentPatient.getFullName());

        VBox placeholderContainer = new VBox();
        placeholderContainer.setAlignment(Pos.CENTER);
        placeholderContainer.setSpacing(20);
        placeholderContainer.setPadding(new Insets(50));

        Label titleLabel = new Label("🆘 Help Desk");
        titleLabel.setStyle(
                "-fx-font-size: 32px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        Label messageLabel = new Label("This feature is coming soon!\nYou will be able to get help and support here.");
        messageLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-text-fill: #7f8c8d; " +
                        "-fx-text-alignment: center;"
        );
        messageLabel.setWrapText(true);

        Button backToDashboard = new Button("Back to Dashboard");
        backToDashboard.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-cursor: hand;"
        );

        backToDashboard.setOnAction(e -> {
            setActiveButton(dashboardBtn);
            showDashboard();
        });

        placeholderContainer.getChildren().addAll(titleLabel, messageLabel, backToDashboard);
        mainContent.getChildren().add(placeholderContainer);
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Handle logout functionality
     */
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Any unsaved changes will be lost.");

        // Style the alert
        alert.getDialogPane().setStyle(
                "-fx-background-color: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 13px;"
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("👋 Patient logged out: " + currentPatient.getFullName());
                System.out.println("📅 Logout Time: " + getCurrentBangladeshTime());

                try {
                    LifeSpringLoginPanelFX loginPanel = new LifeSpringLoginPanelFX();
                    Stage loginStage = new Stage();
                    loginPanel.start(loginStage);

                    primaryStage.close();

                    System.out.println("🔄 Redirected to login window");

                } catch (Exception e) {
                    System.err.println("❌ Error launching login window: " + e.getMessage());
                    primaryStage.close();
                }
            }
        });
    }

    /**
     * Get current Bangladesh time formatted
     */
    private String getCurrentBangladeshTime() {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Dhaka"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
            return now.format(formatter) + " BST";
        } catch (Exception e) {
            System.err.println("❌ Error getting Bangladesh time: " + e.getMessage());
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"));
        }
    }

    /**
     * Get current time static method
     */
    private static String getCurrentTimeStatic() {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Dhaka"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
            return now.format(formatter) + " BST";
        } catch (Exception e) {
            System.err.println("❌ Error getting Bangladesh time: " + e.getMessage());
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"));
        }
    }

    /**
     * Get next 7 days for appointment booking
     */
    private List<LocalDate> getNext7Days() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            dates.add(today.plusDays(i));
        }
        return dates;
    }

    // ========================================
    // MAIN METHODS & LAUNCH UTILITIES
    // ========================================

    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Launch dashboard for specific patient by username
     */
    public static void launchForPatient(String username) {
        try {
            System.out.println("🚀 Launching Patient Dashboard for: " + username);
            PatientDashboard dashboard = new PatientDashboard(username);

            if (dashboard.currentPatient == null) {
                System.err.println("❌ Failed to launch - patient not found");
                return;
            }

            Stage stage = new Stage();
            dashboard.start(stage);
            System.out.println("✅ Patient Dashboard launched successfully");
        } catch (Exception e) {
            System.err.println("❌ Error launching Patient Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Launch dashboard for specific patient by ID
     */
    public static void launchForPatientId(int patientId) {
        try {
            System.out.println("🚀 Launching Patient Dashboard for ID: " + patientId);
            PatientDashboard dashboard = new PatientDashboard(patientId);

            if (dashboard.currentPatient == null) {
                System.err.println("❌ Failed to launch - patient not found");
                return;
            }

            Stage stage = new Stage();
            dashboard.start(stage);
            System.out.println("✅ Patient Dashboard launched successfully");
        } catch (Exception e) {
            System.err.println("❌ Error launching Patient Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Launch dashboard with existing Patient object
     */
    public static void launchWithPatientObject(Patient patient) {
        try {
            System.out.println("🚀 Launching Patient Dashboard with Patient object: " + patient.getFullName());
            PatientDashboard dashboard = new PatientDashboard(patient);
            Stage stage = new Stage();
            dashboard.start(stage);
            System.out.println("✅ Patient Dashboard launched successfully");
        } catch (Exception e) {
            System.err.println("❌ Error launching Patient Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get current patient (for external access)
     */
    public Patient getCurrentPatient() {
        return this.currentPatient;
    }

    /**
     * Set current patient (for external access)
     */
    public void setCurrentPatient(Patient patient) {
        this.currentPatient = patient;
    }

    /**
     * Check if dashboard is ready
     */
    public boolean isReady() {
        return currentPatient != null && patientDAO != null;
    }

    /**
     * Get database connection status
     */
    public boolean isDatabaseConnected() {
        return patientDAO != null && patientDAO.testConnection();
    }

    /**
     * Refresh patient data from database
     */
    public void refreshPatientData() {
        if (currentPatient != null) {
            try {
                Patient refreshedPatient = patientDAO.getPatientById(currentPatient.getId());
                if (refreshedPatient != null) {
                    this.currentPatient = refreshedPatient;
                    System.out.println("✅ Patient data refreshed successfully");
                } else {
                    System.err.println("❌ Failed to refresh patient data");
                }
            } catch (Exception e) {
                System.err.println("❌ Error refreshing patient data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}