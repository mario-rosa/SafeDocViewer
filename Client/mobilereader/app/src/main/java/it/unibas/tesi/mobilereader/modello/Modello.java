package it.unibas.tesi.mobilereader.modello;

import java.util.HashMap;
import java.util.Map;

public final class Modello {

    public static Map<String, Object> mapBean = new HashMap<>();

    private Modello(){ }
    public  static void putBean (String name, Object bean){
        mapBean.put(name, bean);
    }

    public static Object getBean(String name){
        return mapBean.get(name);
    }
}
