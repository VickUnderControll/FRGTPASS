package org.Main;

import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.sql.*;
import java.util.Objects;

public class Main extends Application {

    // URL de conexión para MariaDB
    private static final String URL = "jdbc:mariadb://localhost:3306/";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    FuncionesComunes funcionesComunes=new FuncionesComunes();

    public static void main(String[] args) {
        createDatabaseAndTables();
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        Label usernameLabel = new Label("Nombre de usuario:");
        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("text-field"); // Aplicar estilo
        Label passwordLabel = new Label("Contraseña:");
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("text-field"); // Aplicar estilo
        Button loginButton = new Button("Iniciar sesión");
        loginButton.getStyleClass().add("button"); // Aplicar estilo

        //icono
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/OIG2.png"))));

        VBox vbox = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, loginButton);
        vbox.getStyleClass().add("vbox"); // Aplicar estilo

        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (login(username, password)) {
                primaryStage.close(); // Cerrar la ventana de inicio de sesión
                showPasswordRecords(); // Mostrar la ventana de registros de contraseñas
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Nombre de usuario o contraseña incorrectos.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        Scene scene = new Scene(vbox, 300, 200);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // Cargar el CSS

        primaryStage.setScene(scene);
        primaryStage.setTitle("Inicio de sesión");
        primaryStage.show();
    }
    private void showPasswordRecords() {
        Stage stage = new Stage();
        TableView<PasswordRecord> tableView = new TableView<>();


        // Configurar las columnas
        TableColumn<PasswordRecord, String> titleColumn = new TableColumn<>("Título");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());

        TableColumn<PasswordRecord, String> usernameColumn = new TableColumn<>("Usuario");
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        TableColumn<PasswordRecord, String> passwordColumn = new TableColumn<>("Contraseña");
        passwordColumn.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());

        TableColumn<PasswordRecord, String> emailColumn = new TableColumn<>("Correo");
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        TableColumn<PasswordRecord, String> linkColumn = new TableColumn<>("Enlace");
        linkColumn.setCellValueFactory(cellData -> cellData.getValue().linkProperty());

        tableView.getColumns().addAll(titleColumn, usernameColumn, passwordColumn, emailColumn, linkColumn);

        // Obtener los datos de la tabla "passwords"
        ObservableList<PasswordRecord> records = getPasswordRecords();
        tableView.setItems(records);

        // Botón de agregar registro
        Button addButton = new Button("Agregar Registro");
        addButton.getStyleClass().add("button"); // Aplicar estilo
        addButton.setOnAction(event -> showAddRecordDialog(records));
        tableView.setRowFactory(tv -> {
            TableRow<PasswordRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    PasswordRecord selectedRecord = row.getItem();
                    showRecordDetails(selectedRecord);
                }
            });
            return row;
        });

        // Botón de abrir URL
        Button openUrlButton = new Button("Abrir URL");
        openUrlButton.getStyleClass().add("button"); // Aplicar estilo
        openUrlButton.setOnAction(event -> {
            PasswordRecord selectedRecord = tableView.getSelectionModel().getSelectedItem();
            if (selectedRecord != null) {
                openUrlInBrowser(selectedRecord.getLink(), selectedRecord.getTitle(), selectedRecord.getUsername(), selectedRecord.getEmail());
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Selecciona un registro para abrir la URL.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        //icono
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/OIG2.png"))));

        VBox vbox = new VBox(10, tableView, addButton, openUrlButton);
        vbox.getStyleClass().add("vbox"); // Aplicar estilo
        Scene scene = new Scene(vbox, 700, 500);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // Cargar el CSS
        stage.setScene(scene);
        stage.setTitle("Registros de contraseñas");
        stage.show();
    }

    private void openUrlInBrowser(String link, String title, String username, String email) {
        try {
            if (link != null && !link.isEmpty()) {
                // Abre la URL en el navegador predeterminado
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(link));
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "El enlace está vacío.", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al abrir la URL.", ButtonType.OK);
            alert.showAndWait();
        }
    }
    private void showRecordDetails(PasswordRecord record) {
        if (record == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No se ha seleccionado ningún registro.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        Stage detailStage = new Stage();
        detailStage.setTitle("Detalles del Registro");

        //icono
        detailStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/OIG2.png"))));



        // Crear un GridPane para alinear los elementos
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(15);
        gridPane.setPadding(new javafx.geometry.Insets(10));

        // Crear etiquetas y campos editables
        HBox titleField = createEditableField("Título", record.getTitle());
        HBox usernameField = createEditableField("Usuario", record.getUsername());
        HBox passwordField = createEditableField("Contraseña", funcionesComunes.desencriptaPassword(record.getPassword()));
        HBox emailField = createEditableField("Correo", record.getEmail());
        HBox linkField = createEditableField("Enlace", record.getLink());

        TextField titleTextField = (TextField) titleField.getChildren().get(1);
        TextField usernameTextField = (TextField) usernameField.getChildren().get(1);
        TextField passwordTextField = (TextField) passwordField.getChildren().get(1);
        TextField emailTextField = (TextField) emailField.getChildren().get(1);
        TextField linkTextField = (TextField) linkField.getChildren().get(1);

        titleTextField.setStyle(FuncionesComunes.backgroundNonEditable);
        usernameTextField.setStyle(FuncionesComunes.backgroundNonEditable);
        passwordTextField.setStyle(FuncionesComunes.backgroundNonEditable);
        emailTextField.setStyle(FuncionesComunes.backgroundNonEditable);
        linkTextField.setStyle(FuncionesComunes.backgroundNonEditable);

        // Añadir campos al GridPane
        gridPane.addRow(0, new Label("Título:"), titleTextField);
        gridPane.addRow(1, new Label("Usuario:"), usernameTextField);
        gridPane.addRow(2, new Label("Contraseña:"), passwordTextField);
        gridPane.addRow(3, new Label("Correo:"), emailTextField);
        gridPane.addRow(4, new Label("Enlace:"), linkTextField);

        // Botón para guardar cambios
        Button saveButton = new Button("Guardar");
        saveButton.setVisible(false);
        saveButton.setOnAction(event -> {
            String newTitle = titleTextField.getText();
            String newUsername = usernameTextField.getText();
            String newPassword = funcionesComunes.encriptaPassword(passwordTextField.getText());
            String newEmail = emailTextField.getText();
            String newLink = linkTextField.getText();

            updateRecordInDatabase(record.getId(), newTitle, newUsername, newPassword, newEmail, newLink);
            record.setTitle(newTitle);
            record.setUsername(newUsername);
            record.setPassword(newPassword);
            record.setEmail(newEmail);
            record.setLink(newLink);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Registro actualizado correctamente.", ButtonType.OK);
            alert.showAndWait();
            detailStage.close();
        });

        // Botón para habilitar edición
        Button editButton = new Button("Editar");
        editButton.setOnAction(event -> {
            titleTextField.setEditable(true);
            usernameTextField.setEditable(true);
            passwordTextField.setEditable(true);
            emailTextField.setEditable(true);
            linkTextField.setEditable(true);

            titleTextField.setStyle(FuncionesComunes.backgroundEditable);
            usernameTextField.setStyle(FuncionesComunes.backgroundEditable);
            passwordTextField.setStyle(FuncionesComunes.backgroundEditable);
            emailTextField.setStyle(FuncionesComunes.backgroundEditable);
            linkTextField.setStyle(FuncionesComunes.backgroundEditable);

            editButton.setDisable(true);
            saveButton.setVisible(true);
        });

        // Añadir botones
        HBox buttonBox = new HBox(10, editButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Crear un VBox para contener todo
        VBox vbox = new VBox(15, gridPane, buttonBox);
        vbox.setPadding(new javafx.geometry.Insets(10));

        Scene scene = new Scene(vbox, 500, 300);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // Cargar el CSS
        detailStage.setScene(scene);
        detailStage.show();
    }


    // Método para crear un campo editable
    private HBox createEditableField(String label, String value) {
        Label fieldLabel = new Label(label + ": ");
        TextField valueField = new TextField(value);
        valueField.setEditable(false); // Inicialmente no editable
        valueField.setPrefWidth(300);

        HBox hbox = new HBox(10, fieldLabel, valueField);
        hbox.setSpacing(10);
        return hbox;
    }

    // Método para actualizar el registro en la base de datos
    private void updateRecordInDatabase(int id, String title, String username, String password, String email, String link) {
        try (Connection connection = DriverManager.getConnection(URL + "password_manager", USER, PASSWORD)) {
            String sql = "UPDATE passwords SET title = ?, username = ?, password = ?, email = ?, link = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, username);
                preparedStatement.setString(3, password);
                preparedStatement.setString(4, email);
                preparedStatement.setString(5, link);
                preparedStatement.setInt(6, id);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al actualizar el registro en la base de datos.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // Método para crear un campo copiable
    private HBox createCopyableField(String label, String value) {
        Label fieldLabel = new Label(label + ": ");
        TextField valueField = new TextField(value);
        valueField.setEditable(false); // Campo de solo lectura

        // Botón para copiar al portapapeles
        Button copyButton = new Button("Copiar");
        copyButton.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(value);
            Clipboard.getSystemClipboard().setContent(content);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, label + " copiado al portapapeles.", ButtonType.OK);
            alert.showAndWait();
        });

        HBox hbox = new HBox(10, fieldLabel, valueField, copyButton);
        hbox.setSpacing(10);
        return hbox;
    }


    private ObservableList<PasswordRecord> getPasswordRecords() {
        ObservableList<PasswordRecord> records = FXCollections.observableArrayList();

        try (Connection connection = DriverManager.getConnection(URL + "password_manager", USER, PASSWORD)) {
            String sql = "SELECT id, title, username, password, email, link FROM passwords";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    String email = resultSet.getString("email");
                    String link = resultSet.getString("link");
//
//                    // Convertir el arreglo de bytes a una imagen
//                    ImageView photoView = null;
//                    if (photoBytes != null) {
//                        Image image = new Image(new ByteArrayInputStream(photoBytes));
//                        photoView = new ImageView(image);
//                        photoView.setFitWidth(50); // Ajustar el tamaño de la imagen
//                        photoView.setFitHeight(50);
//                        photoView.setPreserveRatio(true);
//                    }

                    records.add(new PasswordRecord(id, title, username, password, email, link));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    private void showAddRecordDialog(ObservableList<PasswordRecord> records) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Agregar Nuevo Registro");

        TextField titleField = new TextField();
        titleField.setPromptText("Título");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Usuario");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");

        // Botón "Generar" junto al campo de contraseña
        Button generateButton = new Button("Generar");
        generateButton.setOnAction(event -> {
            // Llama a tu método para generar la contraseña
            String generatedPassword = FuncionesComunes.generateRandomPassword();
            passwordField.setText(generatedPassword);
        });

        // HBox para agrupar el campo de contraseña y el botón "Generar"
        HBox passwordBox = new HBox(10, passwordField, generateButton);

        TextField emailField = new TextField();
        emailField.setPromptText("Correo");

        TextField linkField = new TextField();
        linkField.setPromptText("Enlace");
//
//        Button uploadPhotoButton = new Button("Cargar Foto");
//        Label photoLabel = new Label("Foto no seleccionada");
//        final byte[][] photoData = {null};
//
//        uploadPhotoButton.setOnAction(event -> {
//            FileChooser fileChooser = new FileChooser();
//            fileChooser.setTitle("Seleccionar Foto");
//            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
//            File selectedFile = fileChooser.showOpenDialog(dialogStage);
//            if (selectedFile != null) {
//                photoLabel.setText("Foto seleccionada: " + selectedFile.getName());
//                photoData[0] = serializePhoto(selectedFile);
//            }
//        });

        Button saveButton = new Button("Guardar");
        saveButton.setOnAction(event -> {
            String title = titleField.getText();
            String username = usernameField.getText();
            String password = funcionesComunes.encriptaPassword(passwordField.getText());
            String email = emailField.getText();
            String link = linkField.getText();


            if (!title.isEmpty() && !password.isEmpty()) {
                addRecordToDatabase(title, username, password, email, link);
//                ImageView photoView = (photo != null) ? new ImageView(new Image(new ByteArrayInputStream(photo))) : null;
//                if (photoView != null) {
//                    photoView.setFitWidth(50);
//                    photoView.setFitHeight(50);
//                    photoView.setPreserveRatio(true);
//                }
                records.add(new PasswordRecord(0, title, username, password, email, link));
                dialogStage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "El título y la contraseña son obligatorios.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        VBox vbox = new VBox(10,
                new Label("Título:"), titleField,
                new Label("Usuario:"), usernameField,
                new Label("Contraseña:"), passwordBox, // Usa el HBox aquí
                new Label("Correo:"), emailField,
                new Label("Enlace:"), linkField, saveButton);

        Scene scene = new Scene(vbox, 400, 350);
        dialogStage.setScene(scene);
        dialogStage.show();
    }


    private byte[] serializePhoto(File photoFile) {
        try {
            return Files.readAllBytes(photoFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addRecordToDatabase(String title, String username, String password, String email, String link) {
        try (Connection connection = DriverManager.getConnection(URL + "password_manager", USER, PASSWORD)) {
            String sql = "INSERT INTO passwords (user_id, title, username, password, email, link) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int userId = 1; // Reemplaza con el ID del usuario actual
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, username);
                preparedStatement.setString(4, password);
                preparedStatement.setString(5, email);
                preparedStatement.setString(6, link);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createDatabaseAndTables() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS password_manager";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createDatabaseSQL);
            }

            String useDatabaseSQL = "USE password_manager";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(useDatabaseSQL);
            }

            String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL)";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createUsersTableSQL);
            }

            String createPasswordsTableSQL = "CREATE TABLE IF NOT EXISTS passwords (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "username VARCHAR(255), " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255), " +
                    "link VARCHAR(255), " +
                    "FOREIGN KEY (user_id) REFERENCES users(id))";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createPasswordsTableSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean login(String username, String password) {
        try (Connection connection = DriverManager.getConnection(URL + "password_manager", USER, PASSWORD)) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
