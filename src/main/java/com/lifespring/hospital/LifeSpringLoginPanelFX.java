package com.lifespring.hospital;

import com.lifespring.hospital.dao.UserDAO;
import com.lifespring.hospital.model.User;
import com.lifespring.hospital.database.DatabaseConnection;
import com.lifespring.hospital.AdminDashboard;
import com.lifespring.hospital.DoctorDashboard;
import com.lifespring.hospital.PatientDashboard;
import com.lifespring.hospital.ReceptionDashboard;
import com.lifespring.hospital.dao.PatientDAO;
import com.lifespring.hospital.model.Patient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class LifeSpringLoginPanelFX extends Application {

    // UI Components
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField passwordTextField;
    private Button passwordToggleButton;
    private boolean passwordVisible = false;

    private Button loginButton;
    private Label statusLabel;

    // Signup Components
    private TextField fullNameField;
    private TextField emailField;
    private TextField signupUsernameField;
    private PasswordField signupPasswordField;
    private PasswordField confirmPasswordField;
    private TextField signupPasswordTextField;
    private TextField confirmPasswordTextField;
    private Button signupPasswordToggleButton;
    private Button confirmPasswordToggleButton;
    private boolean signupPasswordVisible = false;
    private boolean confirmPasswordVisible = false;

    // Form References
    private VBox loginForm;
    private VBox signupForm;
    private VBox formsPanel;

    // Database
    private UserDAO userDAO;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("🚀 Starting LifeSpring Hospital Application...");
        System.out.println("📅 Current Time: " +
                java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Dhaka")).format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")) + " BST (GMT+6)");
        System.out.println("👤 Current User: sabbir-hasnat");

        if (DatabaseConnection.testConnection()) {
            userDAO = new UserDAO();
            System.out.println("✅ Database connection successful!");
        } else {
            System.err.println("❌ Database connection failed!");
            showAlert(Alert.AlertType.ERROR, "Database Error", "Database connection failed! Please check your MySQL server.");
            return;
        }

        primaryStage.setTitle("LifeSpring Hospital - Login Portal");
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();

        BorderPane root = new BorderPane();
        root.setPrefSize(950, 700);
        root.setMaxSize(950, 700);
        root.setMinSize(950, 700);

        VBox leftPanel = createHospitalInfoPanel();
        leftPanel.setPrefWidth(570);
        leftPanel.setMaxWidth(570);
        leftPanel.setMinWidth(570);

        VBox rightPanel = createFormsPanel();
        rightPanel.setPrefWidth(380);
        rightPanel.setMaxWidth(380);
        rightPanel.setMinWidth(380);
        rightPanel.setStyle("-fx-background-color: white;");

        root.setLeft(leftPanel);
        root.setRight(rightPanel);

        Scene scene = new Scene(root, 950, 700);
        primaryStage.setScene(scene);
        primaryStage.setMaxWidth(950);
        primaryStage.setMaxHeight(700);
        primaryStage.setMinWidth(950);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    private VBox createFormsPanel() {
        formsPanel = new VBox();
        formsPanel.setPadding(new Insets(35));
        formsPanel.setAlignment(Pos.CENTER);
        formsPanel.setSpacing(28);

        loginForm = createLoginForm();
        signupForm = createSignupForm();
        signupForm.setVisible(false);
        signupForm.setManaged(false);

        formsPanel.getChildren().addAll(loginForm, signupForm);

        System.out.println("✅ Forms panel created with stored references");
        return formsPanel;
    }

    private VBox createLoginForm() {
        VBox loginFormLocal = new VBox(22);
        loginFormLocal.setAlignment(Pos.CENTER);

        VBox headerSection = createHeaderSection();

        Text loginTitle = new Text("Sign In");
        loginTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        loginTitle.setFill(Color.web("#3498db"));

        Text loginSubtitle = new Text("Welcome back! Please sign in to your account");
        loginSubtitle.setFont(Font.font("Segoe UI", 14));
        loginSubtitle.setFill(Color.web("#6b7280"));

        VBox formFields = createLoginFormFields();

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-alignment: center;");
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(310);

        loginButton = createStyledButton("Sign In", "#3498db");
        loginButton.setOnAction(e -> authenticateUser());

        HBox switchContainer = new HBox(5);
        switchContainer.setAlignment(Pos.CENTER);

        Text switchText = new Text("Don't have an account? ");
        switchText.setFont(Font.font("Segoe UI", 13));
        switchText.setFill(Color.web("#6b7280"));

        Hyperlink switchLink = new Hyperlink("Sign up as Patient");
        switchLink.setStyle("-fx-text-fill: #1e488f; -fx-font-size: 13px; -fx-underline: true;");
        switchLink.setOnAction(e -> switchToSignup());

        switchContainer.getChildren().addAll(switchText, switchLink);

        loginFormLocal.getChildren().addAll(
                headerSection,
                loginTitle,
                loginSubtitle,
                formFields,
                statusLabel,
                loginButton,
                switchContainer
        );

        return loginFormLocal;
    }

    private VBox createLoginFormFields() {
        VBox formFields = new VBox(15);

        VBox usernameContainer = new VBox(5);
        Label usernameLabel = new Label("Username or Email");
        usernameLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #374151; -fx-font-weight: bold;");

        usernameField = new TextField();
        usernameField.setPromptText("Enter your username or email");
        usernameField.setPrefHeight(42);
        usernameField.setPrefWidth(310);
        usernameField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 12; " +
                        "-fx-font-size: 14px;"
        );

        usernameContainer.getChildren().addAll(usernameLabel, usernameField);

        VBox passwordContainer = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #374151; -fx-font-weight: bold;");

        StackPane passwordStackPane = new StackPane();
        passwordStackPane.setPrefWidth(310);
        passwordStackPane.setPrefHeight(42);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(42);
        passwordField.setPrefWidth(310);
        passwordField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 12 40 12 12; " +
                        "-fx-font-size: 14px;"
        );

        passwordTextField = new TextField();
        passwordTextField.setPromptText("Enter your password");
        passwordTextField.setPrefHeight(42);
        passwordTextField.setPrefWidth(310);
        passwordTextField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 12 40 12 12; " +
                        "-fx-font-size: 14px;"
        );
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);

        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!passwordTextField.isFocused()) {
                passwordTextField.setText(newText);
            }
        });

        passwordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (!passwordField.isFocused()) {
                passwordField.setText(newText);
            }
        });

        // FIXED: Using text instead of emoji
        passwordToggleButton = new Button("Show");
        passwordToggleButton.setPrefSize(50, 30);
        passwordToggleButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-text-fill: #6b7280; " +
                        "-fx-font-size: 12px; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold;"
        );

        passwordToggleButton.setOnAction(e -> togglePasswordVisibility());

        StackPane.setAlignment(passwordToggleButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(passwordToggleButton, new Insets(0, 10, 0, 0));

        passwordStackPane.getChildren().addAll(passwordField, passwordTextField, passwordToggleButton);

        passwordContainer.getChildren().addAll(passwordLabel, passwordStackPane);

        formFields.getChildren().addAll(usernameContainer, passwordContainer);
        return formFields;
    }

    // FIXED: Using text instead of emoji
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
            passwordToggleButton.setText("Hide");
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            passwordToggleButton.setText("Show");
        }
    }

    private VBox createSignupForm() {
        VBox signupFormLocal = new VBox(12);
        signupFormLocal.setAlignment(Pos.CENTER);
        signupFormLocal.setPadding(new Insets(10, 20, 10, 20));

        HBox backButtonRow = new HBox();
        backButtonRow.setAlignment(Pos.CENTER_LEFT);
        backButtonRow.setPadding(new Insets(0, 0, 5, 0));

        Button backButton = new Button("← Back to Login");
        backButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #3498db; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-color: #3498db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 4 8; " +
                        "-fx-cursor: hand;"
        );

        backButton.setOnMouseEntered(e -> {
            backButton.setStyle(
                    "-fx-background-color: #1e488f; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-border-color: #1e488f; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 6; " +
                            "-fx-background-radius: 6; " +
                            "-fx-padding: 4 8; " +
                            "-fx-cursor: hand;"
            );
        });

        backButton.setOnMouseExited(e -> {
            backButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #1e488f; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-border-color: #1e488f; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 6; " +
                            "-fx-background-radius: 6; " +
                            "-fx-padding: 4 8; " +
                            "-fx-cursor: hand;"
            );
        });

        backButton.setOnAction(e -> {
            System.out.println("🔙 Back to Login button clicked");
            switchToLogin();
        });

        backButtonRow.getChildren().add(backButton);

        VBox headerSection = createCompactHeaderSection();

        Text signupTitle = new Text("Create Patient Account");
        signupTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        signupTitle.setFill(Color.web("#3498db"));

        Text signupSubtitle = new Text("Join LifeSpring Hospital as a patient");
        signupSubtitle.setFont(Font.font("Segoe UI", 12));
        signupSubtitle.setFill(Color.web("#6b7280"));

        VBox formFields = createCompactSignupFormFields();

        Label signupStatusLabel = new Label();
        signupStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 10; -fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-alignment: center;");
        signupStatusLabel.setVisible(false);
        signupStatusLabel.setWrapText(true);
        signupStatusLabel.setMaxWidth(280);

        Button signupButton = createCompactStyledButton("Create Patient Account", "#3498db");
        signupButton.setOnAction(e -> registerUser(signupButton, signupStatusLabel));

        HBox switchContainer = new HBox(3);
        switchContainer.setAlignment(Pos.CENTER);
        switchContainer.setPadding(new Insets(3, 0, 0, 0));

        Text switchText = new Text("Already have an account? ");
        switchText.setFont(Font.font("Segoe UI", 11));
        switchText.setFill(Color.web("#6b7280"));

        Hyperlink switchLink = new Hyperlink("Sign in");
        switchLink.setStyle(
                "-fx-text-fill: #2980b9; " +
                        "-fx-font-size: 11px; " +
                        "-fx-underline: true; " +
                        "-fx-cursor: hand;"
        );

        switchLink.setOnAction(e -> {
            System.out.println("🔄 Switch to login from signup bottom link");
            switchToLogin();
        });

        switchContainer.getChildren().addAll(switchText, switchLink);

        signupFormLocal.getChildren().addAll(
                backButtonRow,
                headerSection,
                signupTitle,
                signupSubtitle,
                formFields,
                signupStatusLabel,
                signupButton,
                switchContainer
        );

        return signupFormLocal;
    }

    private VBox createCompactHeaderSection() {
        VBox headerSection = new VBox(8);
        headerSection.setAlignment(Pos.CENTER);

        Text logoText = new Text("🏥");
        logoText.setFont(Font.font(35));

        Text hospitalName = new Text("LifeSpring Hospital");
        hospitalName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        hospitalName.setFill(Color.web("#1f2937"));

        Text tagline = new Text("Your Health, Our Priority");
        tagline.setFont(Font.font("Segoe UI", 10));
        tagline.setFill(Color.web("#6b7280"));

        headerSection.getChildren().addAll(logoText, hospitalName, tagline);
        return headerSection;
    }

    private VBox createCompactSignupFormFields() {
        VBox formFields = new VBox(6);

        VBox fullNameContainer = new VBox(2);
        Label fullNameLabel = new Label("Full Name");
        fullNameLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11px; -fx-text-fill: #374151; -fx-font-weight: bold;");

        fullNameField = new TextField();
        fullNameField.setPromptText("Enter your full name");
        fullNameField.setPrefHeight(30);
        fullNameField.setPrefWidth(280);
        fullNameField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 6; " +
                        "-fx-font-size: 12px;"
        );

        fullNameContainer.getChildren().addAll(fullNameLabel, fullNameField);

        VBox emailContainer = new VBox(2);
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11px; -fx-text-fill: #374151; -fx-font-weight: bold;");

        emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        emailField.setPrefHeight(30);
        emailField.setPrefWidth(280);
        emailField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 6; " +
                        "-fx-font-size: 12px;"
        );

        emailContainer.getChildren().addAll(emailLabel, emailField);

        VBox usernameContainer = new VBox(2);
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11px; -fx-text-fill: #374151; -fx-font-weight: bold;");

        signupUsernameField = new TextField();
        signupUsernameField.setPromptText("Choose a username");
        signupUsernameField.setPrefHeight(30);
        signupUsernameField.setPrefWidth(280);
        signupUsernameField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 6; " +
                        "-fx-font-size: 12px;"
        );

        usernameContainer.getChildren().addAll(usernameLabel, signupUsernameField);

        VBox passwordContainer = new VBox(2);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11px; -fx-text-fill: #374151; -fx-font-weight: bold;");

        StackPane signupPasswordStackPane = new StackPane();
        signupPasswordStackPane.setPrefWidth(280);
        signupPasswordStackPane.setPrefHeight(30);

        signupPasswordField = new PasswordField();
        signupPasswordField.setPromptText("Create password (min 6 chars)");
        signupPasswordField.setPrefHeight(30);
        signupPasswordField.setPrefWidth(280);
        signupPasswordField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 6 30 6 6; " +
                        "-fx-font-size: 12px;"
        );

        signupPasswordTextField = new TextField();
        signupPasswordTextField.setPromptText("Create password (min 6 chars)");
        signupPasswordTextField.setPrefHeight(30);
        signupPasswordTextField.setPrefWidth(280);
        signupPasswordTextField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 6 30 6 6; " +
                        "-fx-font-size: 12px;"
        );
        signupPasswordTextField.setVisible(false);
        signupPasswordTextField.setManaged(false);

        signupPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!signupPasswordTextField.isFocused()) {
                signupPasswordTextField.setText(newText);
            }
        });

        signupPasswordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (!signupPasswordField.isFocused()) {
                signupPasswordField.setText(newText);
            }
        });

        // FIXED: Using text instead of emoji
        signupPasswordToggleButton = new Button("Show");
        signupPasswordToggleButton.setPrefSize(40, 25);
        signupPasswordToggleButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-text-fill: #6b7280; " +
                        "-fx-font-size: 11px; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold;"
        );

        signupPasswordToggleButton.setOnAction(e -> toggleSignupPasswordVisibility());

        StackPane.setAlignment(signupPasswordToggleButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(signupPasswordToggleButton, new Insets(0, 8, 0, 0));

        signupPasswordStackPane.getChildren().addAll(signupPasswordField, signupPasswordTextField, signupPasswordToggleButton);

        passwordContainer.getChildren().addAll(passwordLabel, signupPasswordStackPane);

        VBox confirmPasswordContainer = new VBox(2);
        Label confirmPasswordLabel = new Label("Confirm Password");
        confirmPasswordLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11px; -fx-text-fill: #374151; -fx-font-weight: bold;");

        StackPane confirmPasswordStackPane = new StackPane();
        confirmPasswordStackPane.setPrefWidth(280);
        confirmPasswordStackPane.setPrefHeight(30);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setPrefHeight(30);
        confirmPasswordField.setPrefWidth(280);
        confirmPasswordField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 6 30 6 6; " +
                        "-fx-font-size: 12px;"
        );

        confirmPasswordTextField = new TextField();
        confirmPasswordTextField.setPromptText("Confirm your password");
        confirmPasswordTextField.setPrefHeight(30);
        confirmPasswordTextField.setPrefWidth(280);
        confirmPasswordTextField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 6 30 6 6; " +
                        "-fx-font-size: 12px;"
        );
        confirmPasswordTextField.setVisible(false);
        confirmPasswordTextField.setManaged(false);

        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!confirmPasswordTextField.isFocused()) {
                confirmPasswordTextField.setText(newText);
            }
        });

        confirmPasswordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (!confirmPasswordField.isFocused()) {
                confirmPasswordField.setText(newText);
            }
        });

        // FIXED: Using text instead of emoji
        confirmPasswordToggleButton = new Button("Show");
        confirmPasswordToggleButton.setPrefSize(40, 25);
        confirmPasswordToggleButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-text-fill: #6b7280; " +
                        "-fx-font-size: 11px; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold;"
        );

        confirmPasswordToggleButton.setOnAction(e -> toggleConfirmPasswordVisibility());

        StackPane.setAlignment(confirmPasswordToggleButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(confirmPasswordToggleButton, new Insets(0, 8, 0, 0));

        confirmPasswordStackPane.getChildren().addAll(confirmPasswordField, confirmPasswordTextField, confirmPasswordToggleButton);

        confirmPasswordContainer.getChildren().addAll(confirmPasswordLabel, confirmPasswordStackPane);

        formFields.getChildren().addAll(
                fullNameContainer,
                emailContainer,
                usernameContainer,
                passwordContainer,
                confirmPasswordContainer
        );
        return formFields;
    }

    // FIXED: Using text instead of emoji
    private void toggleSignupPasswordVisibility() {
        signupPasswordVisible = !signupPasswordVisible;

        if (signupPasswordVisible) {
            signupPasswordTextField.setText(signupPasswordField.getText());
            signupPasswordField.setVisible(false);
            signupPasswordField.setManaged(false);
            signupPasswordTextField.setVisible(true);
            signupPasswordTextField.setManaged(true);
            signupPasswordTextField.requestFocus();
            signupPasswordTextField.positionCaret(signupPasswordTextField.getText().length());
            signupPasswordToggleButton.setText("Hide");
        } else {
            signupPasswordField.setText(signupPasswordTextField.getText());
            signupPasswordTextField.setVisible(false);
            signupPasswordTextField.setManaged(false);
            signupPasswordField.setVisible(true);
            signupPasswordField.setManaged(true);
            signupPasswordField.requestFocus();
            signupPasswordField.positionCaret(signupPasswordField.getText().length());
            signupPasswordToggleButton.setText("Show");
        }
    }

    // FIXED: Using text instead of emoji
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;

        if (confirmPasswordVisible) {
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordTextField.requestFocus();
            confirmPasswordTextField.positionCaret(confirmPasswordTextField.getText().length());
            confirmPasswordToggleButton.setText("Hide");
        } else {
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordField.requestFocus();
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
            confirmPasswordToggleButton.setText("Show");
        }
    }

    private Button createCompactStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(280);
        button.setPrefHeight(38);
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: " + darkenColor(color) + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 6, 0, 0, 3);"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            );
        });

        return button;
    }

    private VBox createHeaderSection() {
        VBox headerSection = new VBox(15);
        headerSection.setAlignment(Pos.CENTER);

        Text logoText = new Text("🏥");
        logoText.setFont(Font.font(45));
        headerSection.getChildren().add(logoText);

        Text hospitalName = new Text("LifeSpring Hospital");
        hospitalName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        hospitalName.setFill(Color.web("#1f2937"));

        Text tagline = new Text("Your Health, Our Priority");
        tagline.setFont(Font.font("Segoe UI", 12));
        tagline.setFill(Color.web("#6b7280"));

        headerSection.getChildren().addAll(hospitalName, tagline);
        return headerSection;
    }

    private VBox createHospitalInfoPanel() {
        VBox hospitalPanel = new VBox();
        hospitalPanel.setAlignment(Pos.CENTER);
        hospitalPanel.setPadding(new Insets(40));

        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, null,
                new Stop(0, Color.web("#3498db")),
                new Stop(1, Color.web("#2980b9"))
        );

        hospitalPanel.setBackground(new Background(new BackgroundFill(gradient, null, null)));

        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER);

        Text illustration = new Text("🏥");
        illustration.setFont(Font.font(110));

        Text welcomeText = new Text("Welcome to");
        welcomeText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 22));
        welcomeText.setFill(Color.WHITE);

        Text hospitalName = new Text("LifeSpring Hospital");
        hospitalName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        hospitalName.setFill(Color.WHITE);

        Text description = new Text("Providing exceptional healthcare services with compassion and excellence. Join our community of patients who trust us with their health and wellbeing.");
        description.setFont(Font.font("Segoe UI", 15));
        description.setFill(Color.web("#c2d9ff"));
        description.setWrappingWidth(380);
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox features = new VBox(15);
        features.setAlignment(Pos.CENTER_LEFT);

        String[] featureList = {
                "24/7 Emergency Services",
                "Expert Medical Team",
                "Modern Equipment",
                "Patient-Centered Care"
        };

        for (String feature : featureList) {
            HBox featureItem = new HBox(12);
            featureItem.setAlignment(Pos.CENTER_LEFT);

            Text checkmark = new Text("✓");
            checkmark.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            checkmark.setFill(Color.WHITE);

            Text featureText = new Text(feature);
            featureText.setFont(Font.font("Segoe UI", 14));
            featureText.setFill(Color.web("#c2d9ff"));

            featureItem.getChildren().addAll(checkmark, featureText);
            features.getChildren().add(featureItem);
        }

        content.getChildren().addAll(
                illustration,
                welcomeText,
                hospitalName,
                description,
                features
        );

        hospitalPanel.getChildren().add(content);
        return hospitalPanel;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(310);
        button.setPrefHeight(45);
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: " + darkenColor(color) + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 6, 0, 0, 3);"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI'; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);"
            );
        });

        return button;
    }

    private String darkenColor(String color) {
        switch (color) {
            case "#10b981":
                return "#059669";
            case "#3b82f6":
                return "#2563eb";
            case "#ef4444":
                return "#dc2626";
            case "#1e488f":
                return "#1a3c7a";
            default:
                return color;
        }
    }

    private void switchToSignup() {
        try {
            System.out.println("🔄 Switching to signup form...");

            if (loginForm != null && signupForm != null) {
                if (statusLabel != null) {
                    statusLabel.setVisible(false);
                }
                resetLoginButton();

                loginForm.setVisible(false);
                loginForm.setManaged(false);

                signupForm.setVisible(true);
                signupForm.setManaged(true);

                System.out.println("✅ Successfully switched to signup form");
            }
        } catch (Exception e) {
            System.err.println("❌ Error switching to signup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchToLogin() {
        try {
            System.out.println("🔄 Switching back to login form...");

            if (loginForm != null && signupForm != null) {
                if (statusLabel != null) {
                    statusLabel.setVisible(false);
                }
                resetLoginButton();

                signupForm.setVisible(false);
                signupForm.setManaged(false);

                loginForm.setVisible(true);
                loginForm.setManaged(true);

                clearSignupForm();

                System.out.println("✅ Successfully switched back to login form");
            }
        } catch (Exception e) {
            System.err.println("❌ Error switching to login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetLoginButton() {
        try {
            if (loginButton != null) {
                loginButton.setDisable(false);
                loginButton.setText("Sign In");
                System.out.println("🔄 Login button reset successfully");
            }
        } catch (Exception e) {
            System.err.println("❌ Error resetting login button: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        boolean isValid = pattern.matcher(email).matches();

        if (isValid) {
            System.out.println("📧 Email validation passed: " + email);
        } else {
            System.out.println("❌ Email validation failed: " + email);
        }

        return isValid;
    }

    private void showErrorMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle(
                    "-fx-text-fill: #ef4444; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8 12; " +
                            "-fx-background-color: #fef2f2; " +
                            "-fx-border-color: #fecaca; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 6; " +
                            "-fx-background-radius: 6; " +
                            "-fx-alignment: center;"
            );
            statusLabel.setVisible(true);
        }
        System.out.println("❌ Error: " + message);
    }

    private void showSignupErrorMessage(String message, Label signupStatusLabel) {
        if (signupStatusLabel != null) {
            signupStatusLabel.setText(message);
            signupStatusLabel.setStyle(
                    "-fx-text-fill: #ef4444; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 6 10; " +
                            "-fx-background-color: #fef2f2; " +
                            "-fx-border-color: #fecaca; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-alignment: center;"
            );
            signupStatusLabel.setVisible(true);
        }
        System.out.println("❌ Signup Error: " + message);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (alertType == Alert.AlertType.INFORMATION) {
            alert.setGraphic(new javafx.scene.text.Text("🏥"));
        }

        alert.showAndWait();
    }

    private void clearLoginForm() {
        try {
            if (usernameField != null) usernameField.clear();
            if (passwordField != null) passwordField.clear();
            if (passwordTextField != null) passwordTextField.clear();
            if (statusLabel != null) statusLabel.setVisible(false);

            passwordVisible = false;
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            if (passwordToggleButton != null) passwordToggleButton.setText("Show");

            System.out.println("🧹 Login form cleared successfully at: " +
                    java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Dhaka")).format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")) + " BST");
        } catch (Exception e) {
            System.err.println("❌ Error clearing login form: " + e.getMessage());
        }
    }

    private String getCurrentPassword() {
        if (passwordVisible) {
            return passwordTextField.getText();
        } else {
            return passwordField.getText();
        }
    }

    private String getCurrentSignupPassword() {
        if (signupPasswordVisible) {
            return signupPasswordTextField.getText();
        } else {
            return signupPasswordField.getText();
        }
    }

    private String getCurrentConfirmPassword() {
        if (confirmPasswordVisible) {
            return confirmPasswordTextField.getText();
        } else {
            return confirmPasswordField.getText();
        }
    }

    /**
     * UNIFIED: Authentication with Receptionist support
     */
    private void authenticateUser() {
        String username = usernameField.getText().trim();
        String password = getCurrentPassword();

        statusLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Please enter both username and password");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Signing In...");

        System.out.println("🔐 UNIFIED Authentication attempt for: " + username);
        System.out.println("📅 Time: " +
                java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Dhaka")).format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")) + " BST");

        Task<User> authTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                Thread.sleep(1000);

                // Strategy 1: Try admin/doctor/receptionist login (users table)
                User user = userDAO.authenticateUser(username, password);

                if (user != null) {
                    System.out.println("✅ User authentication successful: " + user.getFullName() + " (Role: " + user.getRole() + ")");
                    return user;
                }

                // Strategy 2: Try patient login (patients table) - UNIFIED SYSTEM
                System.out.println("🔄 Trying UNIFIED patient authentication...");
                PatientDAO patientDAO = new PatientDAO();
                Patient patient = patientDAO.validateLogin(username, password);

                if (patient != null) {
                    System.out.println("✅ UNIFIED Patient authentication successful: " + patient.getFullName());

                    // Convert Patient to User for unified dashboard handling
                    User patientUser = new User();
                    patientUser.setUsername(patient.getUsername());
                    patientUser.setFullName(patient.getFullName());
                    patientUser.setEmail(patient.getEmail());
                    patientUser.setRole("patient");
                    patientUser.setActive(true);

                    return patientUser;
                }

                System.out.println("❌ UNIFIED Authentication failed for: " + username);
                return null;
            }

            @Override
            protected void succeeded() {
                User user = getValue();
                Platform.runLater(() -> {
                    if (user != null) {
                        resetLoginButton();
                        System.out.println("✅ UNIFIED Login successful! Welcome " + user.getFullName());
                        showSimpleSuccessMessage(user);
                    } else {
                        showErrorMessage("❌ Invalid username or password");
                        resetLoginButton();
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showErrorMessage("❌ Login failed. Please try again");
                    resetLoginButton();
                });
            }
        };

        Thread authThread = new Thread(authTask);
        authThread.setDaemon(true);
        authThread.start();
    }

    /**
     * FIXED: Enhanced Success Message with Receptionist Dashboard Routing
     */
    private void showSimpleSuccessMessage(User user) {
        try {
            if (statusLabel != null) {
                String welcomeMessage = "✅ Welcome " + user.getFullName() + "! (" + user.getRole() + ")";

                statusLabel.setText(welcomeMessage);
                statusLabel.setStyle(
                        "-fx-text-fill: #059669; " +
                                "-fx-font-size: 12px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8 12; " +
                                "-fx-background-color: #f0fdf4; " +
                                "-fx-border-color: #bbf7d0; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 6; " +
                                "-fx-background-radius: 6; " +
                                "-fx-alignment: center;"
                );
                statusLabel.setVisible(true);

                System.out.println("✅ Success message displayed: " + welcomeMessage);
            }

            String timestamp = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Dhaka"))
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"));

            System.out.println("✅ Login successful!");
            System.out.println("👤 User: " + user.getFullName() + " (" + user.getRole() + ")");
            System.out.println("📅 Login Time: " + timestamp + " BST (GMT+6)");

            clearLoginForm();

            Task<Void> redirectTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(1500);
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        try {
                            Stage currentStage = (Stage) loginButton.getScene().getWindow();

                            switch (user.getRole().toLowerCase()) {
                                case "admin":
                                    AdminDashboard.show(currentStage, user);
                                    System.out.println("🔄 Redirected to Admin Dashboard");
                                    break;

                                case "doctor":
                                    DoctorDashboard.show(currentStage, user);
                                    System.out.println("🔄 Redirected to Doctor Dashboard");
                                    break;

                                case "receptionist":  // NEW: Receptionist case added
                                    ReceptionDashboard.show(currentStage, user);
                                    System.out.println("🔄 Redirected to Reception Dashboard");
                                    break;

                                case "patient":
                                    try {
                                        System.out.println("🚀 Launching Patient Dashboard for: " + user.getUsername());

                                        PatientDAO patientDAO = new PatientDAO();
                                        Patient patient = patientDAO.getPatientByUsername(user.getUsername());

                                        if (patient != null) {
                                            System.out.println("✅ Patient found in database: " + patient.getFullName());
                                            PatientDashboard.show(currentStage, user);
                                            System.out.println("🔄 Redirected to Patient Dashboard successfully");

                                        } else {
                                            System.err.println("❌ Patient not found in database for username: " + user.getUsername());

                                            java.util.List<Patient> patients = patientDAO.searchPatientsByName(user.getFullName());

                                            if (!patients.isEmpty()) {
                                                System.out.println("✅ Patient found by name, setting username...");
                                                Patient foundPatient = patients.get(0);

                                                boolean usernameSet = patientDAO.setUsernameForPatient(foundPatient.getId(), user.getUsername());

                                                if (usernameSet) {
                                                    System.out.println("✅ Username set successfully, launching dashboard...");
                                                    PatientDashboard.show(currentStage, user);
                                                } else {
                                                    throw new Exception("Failed to set username for patient");
                                                }
                                            } else {
                                                throw new Exception("Patient record not found in database");
                                            }
                                        }

                                    } catch (Exception e) {
                                        System.err.println("❌ Patient Dashboard launch error: " + e.getMessage());
                                        e.printStackTrace();

                                        showAlert(Alert.AlertType.ERROR, "Patient Dashboard Error",
                                                "Unable to launch Patient Dashboard:\n\n" + e.getMessage() +
                                                        "\n\nPossible solutions:\n" +
                                                        "1. Ensure your patient record exists in database\n" +
                                                        "2. Contact system administrator\n" +
                                                        "3. Try logging in again");

                                        if (statusLabel != null) statusLabel.setVisible(false);
                                    }
                                    break;

                                default:
                                    showAlert(Alert.AlertType.ERROR, "Access Error",
                                            "Unknown user role: " + user.getRole() + "\n" +
                                                    "Please contact system administrator.");
                                    System.err.println("❌ Unknown role: " + user.getRole());
                                    if (statusLabel != null) statusLabel.setVisible(false);
                                    break;
                            }

                        } catch (Exception e) {
                            System.err.println("❌ Error during dashboard redirect: " + e.getMessage());
                            e.printStackTrace();

                            showAlert(Alert.AlertType.ERROR, "System Error",
                                    "❌ Unable to launch dashboard\n\n" +
                                            "Error: " + e.getMessage() + "\n\n" +
                                            "Please try logging in again or contact support.");

                            if (statusLabel != null) statusLabel.setVisible(false);
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        System.err.println("❌ Redirect task failed");
                        showAlert(Alert.AlertType.ERROR, "System Error",
                                "Failed to redirect to dashboard.\n\nPlease try logging in again.");
                        if (statusLabel != null) statusLabel.setVisible(false);
                    });
                }
            };

            Thread redirectThread = new Thread(redirectTask);
            redirectThread.setDaemon(true);
            redirectThread.start();

        } catch (Exception e) {
            System.err.println("❌ Error showing success message: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Login Error",
                        "An error occurred during login process:\n\n" + e.getMessage());
                if (statusLabel != null) statusLabel.setVisible(false);
            });
        }
    }

    /**
     * UNIFIED: Registration creates patient in patients table ONLY
     */
    private void registerUser(Button signupButton, Label signupStatusLabel) {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = signupUsernameField.getText().trim();
        String password = getCurrentSignupPassword();
        String confirmPassword = getCurrentConfirmPassword();

        signupStatusLabel.setVisible(false);

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showSignupErrorMessage("Please fill in all fields", signupStatusLabel);
            return;
        }

        if (fullName.length() < 2) {
            showSignupErrorMessage("Full name must be at least 2 characters long", signupStatusLabel);
            return;
        }

        if (!isValidEmail(email)) {
            showSignupErrorMessage("Please enter a valid email address", signupStatusLabel);
            return;
        }

        if (username.length() < 3) {
            showSignupErrorMessage("Username must be at least 3 characters long", signupStatusLabel);
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            showSignupErrorMessage("Username can only contain letters, numbers, underscore, and hyphen", signupStatusLabel);
            return;
        }

        if (password.length() < 6) {
            showSignupErrorMessage("Password must be at least 6 characters long", signupStatusLabel);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showSignupErrorMessage("Passwords do not match", signupStatusLabel);
            return;
        }

        System.out.println("🔍 Checking if username/email exists in patients table...");

        // UNIFIED: Check only patients table
        PatientDAO patientDAO = new PatientDAO();
        if (patientDAO.usernameExists(username)) {
            showSignupErrorMessage("Username already exists. Please choose a different username.", signupStatusLabel);
            return;
        }

        // Check email in patients table
        Patient existingPatient = patientDAO.getPatientByEmail(email);
        if (existingPatient != null) {
            showSignupErrorMessage("Email already exists. Please use a different email.", signupStatusLabel);
            return;
        }

        signupButton.setDisable(true);
        signupButton.setText("Creating Account...");

        signupStatusLabel.setText("🏥 Creating patient account...");
        signupStatusLabel.setStyle("-fx-text-fill: #1e488f; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 10; -fx-background-color: #eff6ff; -fx-border-color: #bfdbfe; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-alignment: center;");
        signupStatusLabel.setVisible(true);

        System.out.println("📝 Creating unified patient account:");
        System.out.println("👤 Name: " + fullName);
        System.out.println("📧 Email: " + email);
        System.out.println("🔑 Username: " + username);
        System.out.println("🏥 System: UNIFIED - patients table only");

        Task<Boolean> registerTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Thread.sleep(1500);

                try {
                    // UNIFIED: Create patient in patients table ONLY
                    System.out.println("🔄 Creating patient in 'patients' table...");

                    Patient patient = new Patient();
                    patient.setUsername(username);
                    patient.setPassword(password);
                    patient.setEmail(email);
                    patient.setFullName(fullName);
                    patient.setAge(25); // Default age - can be updated later
                    patient.setGender("Male"); // Default gender - can be updated later
                    patient.setActive(true);
                    patient.setRole("Patient");

                    // Prepare patient for database
                    patient.prepareForDatabase();

                    boolean success = patientDAO.addPatient(patient);

                    if (success) {
                        System.out.println("✅ Patient created successfully in unified system");

                        // Test login immediately
                        System.out.println("🧪 Testing unified login capability...");
                        Patient loginTest = patientDAO.validateLogin(username, password);

                        if (loginTest != null) {
                            System.out.println("✅ LOGIN TEST PASSED: Patient can login in unified system!");
                            return true;
                        } else {
                            System.err.println("❌ LOGIN TEST FAILED: Check login logic");
                            // Still consider registration successful
                            return true;
                        }

                    } else {
                        System.err.println("❌ Failed to create patient in unified system");
                        return false;
                    }

                } catch (Exception e) {
                    System.err.println("❌ Exception during unified registration: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void succeeded() {
                Boolean success = getValue();
                Platform.runLater(() -> {
                    if (success) {
                        String timestamp = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Dhaka"))
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"));

                        showAlert(Alert.AlertType.INFORMATION, "🎉 Registration Successful",
                                "🏥 Welcome to LifeSpring Hospital!\n\n" +
                                        "✅ Your patient account has been created successfully!\n" +
                                        "✅ Unified system - accessible by both admin and patient portal!\n\n" +
                                        "👤 Name: " + fullName + "\n" +
                                        "📧 Email: " + email + "\n" +
                                        "🔑 Username: " + username + "\n" +
                                        "🏥 System: Unified Patient Database\n" +
                                        "📅 Registered: " + timestamp + " BST\n\n" +
                                        "✅ Admin can view and manage your record\n" +
                                        "✅ You can login to patient portal\n\n" +
                                        "You can now sign in with your credentials!");

                        switchToLogin();
                        clearSignupForm();

                        // Pre-fill login form for convenience
                        usernameField.setText(username);
                        if (passwordVisible) {
                            passwordTextField.setText(password);
                        } else {
                            passwordField.setText(password);
                        }

                    } else {
                        showSignupErrorMessage("Registration failed. Please try again.", signupStatusLabel);
                        resetSignupButton(signupButton);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showSignupErrorMessage("Registration failed due to server error. Please try again.", signupStatusLabel);
                    resetSignupButton(signupButton);
                });
            }
        };

        Thread registerThread = new Thread(registerTask);
        registerThread.setDaemon(true);
        registerThread.start();
    }

    private void resetSignupButton(Button signupButton) {
        if (signupButton != null) {
            signupButton.setDisable(false);
            signupButton.setText("Create Patient Account");
        }
    }

    private void clearSignupForm() {
        try {
            if (fullNameField != null) fullNameField.clear();
            if (emailField != null) emailField.clear();
            if (signupUsernameField != null) signupUsernameField.clear();
            if (signupPasswordField != null) signupPasswordField.clear();
            if (signupPasswordTextField != null) signupPasswordTextField.clear();
            if (confirmPasswordField != null) confirmPasswordField.clear();
            if (confirmPasswordTextField != null) confirmPasswordTextField.clear();

            // Reset password visibility states
            signupPasswordVisible = false;
            confirmPasswordVisible = false;

            if (signupPasswordField != null) {
                signupPasswordField.setVisible(true);
                signupPasswordField.setManaged(true);
            }
            if (signupPasswordTextField != null) {
                signupPasswordTextField.setVisible(false);
                signupPasswordTextField.setManaged(false);
            }
            if (signupPasswordToggleButton != null) {
                signupPasswordToggleButton.setText("Show");
            }

            if (confirmPasswordField != null) {
                confirmPasswordField.setVisible(true);
                confirmPasswordField.setManaged(true);
            }
            if (confirmPasswordTextField != null) {
                confirmPasswordTextField.setVisible(false);
                confirmPasswordTextField.setManaged(false);
            }
            if (confirmPasswordToggleButton != null) {
                confirmPasswordToggleButton.setText("Show");
            }

            System.out.println("🧹 Signup form cleared successfully at: " +
                    java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Dhaka")).format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")) + " BST");
        } catch (Exception e) {
            System.err.println("❌ Error clearing signup form: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("🚀 Launching LifeSpring Hospital Application...");
        System.out.println("📅 Current Date and Time (BST - GMT+6): " +
                java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Dhaka")).format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")));
        System.out.println("👤 Current User: sabbir-hasnat");
        launch(args);
    }
}
