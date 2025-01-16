package org.Main;

import javafx.beans.property.*;
import javafx.scene.image.ImageView;

public class PasswordRecord {
    private  IntegerProperty id;
    private  StringProperty title;
    private  StringProperty username;
    private  StringProperty password;
    private  StringProperty email;
    private  StringProperty link;
   // private  ObjectProperty<ImageView> photo;

    public PasswordRecord(int id, String title, String username, String password, String email, String link) {
       if(id!=0){
           this.id = new SimpleIntegerProperty(id);
       }
        this.title = new SimpleStringProperty(title);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.email = new SimpleStringProperty(email);
        this.link = new SimpleStringProperty(link);
       // this.photo = new SimpleObjectProperty<>(photo);
    }

    // Getters para propiedades
    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public void setLink(String link) {
        this.link.set(link);
    }
//
//    public void setPhoto(ImageView photo) {
//        this.photo.set(photo);
//    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty linkProperty() {
        return link;
    }

//    public ObjectProperty<ImageView> photoProperty() {
//        return photo;
//    }

    // Métodos adicionales si necesitas acceder directamente a los valores
    public String getTitle() {
        return title.get();
    }

    public String getUsername() {
        return username.get();
    }

    public String getPassword() {
        return password.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getLink() {
        return link.get();
    }
//
//    public ImageView getPhoto() {
//        return photo.get();
//    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }
}
