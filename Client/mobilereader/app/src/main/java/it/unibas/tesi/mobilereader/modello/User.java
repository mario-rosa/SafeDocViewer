package it.unibas.tesi.mobilereader.modello;

public class User {

    private String _id;
    private String username;
    private String password;
    private int privileges;

    public User(String _id, String username, String password, int privileges) {
        this._id = _id;
        this.username = username;
        this.password = password;
        this.privileges = privileges;
    }

    public String get_id() {
        return _id;
    }
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPrivileges() {
        return privileges;
    }
}
