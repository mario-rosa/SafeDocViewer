package it.unibas.tesi.mobilereader.modello;

import androidx.annotation.NonNull;

import java.util.Date;

public class Documento {

    private int id;
    private String name;
    private Date expires;
    private String password;
    private String path;

    public Documento(int id, String name, Date expires) {
        this.id = id;
        this.name = name;
        this.expires = expires;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return name;
    }

    public Date getScadenza() {
        return expires;
    }

    public String getPassword() {
        return password;
    }

    public String getPath() {
        return path;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @NonNull
    @Override
    public String toString() {
        return "Id: " + id + " Nome: " + name + " Scadenza: " + expires;
    }
}
