package it.unibas.tesi.mobilereader;

import java.net.CookieManager;
import it.unibas.tesi.mobilereader.modello.User;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;

public final class Constants{
    public static String basicUrl = "https://bfde-79-31-56-227.ngrok.io";
    public static OkHttpClient CLIENT = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(new CookieManager())).build();
    public static User user = null;
}

