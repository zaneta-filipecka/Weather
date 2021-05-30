package pl.wsiz.pogoda.API_Pogoda;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class API_Pogoda {

    public static final String APP_ID = "2fc664f69f39eb15c16c7b5bd8fa8684";
    public static Location lokalizacja = null;

    public static String convertUnixToDate(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy (EEE)");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String convertUnixToHour(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formatted = sdf.format(date);
        return formatted;
    }
}
