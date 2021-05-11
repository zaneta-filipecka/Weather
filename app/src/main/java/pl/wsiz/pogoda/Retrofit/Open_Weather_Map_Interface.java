package pl.wsiz.pogoda.Retrofit;

import io.reactivex.Observable;
import pl.wsiz.pogoda.Model.WeatherResult;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Open_Weather_Map_Interface {

    @GET("weather")

    Observable<WeatherResult> getWeatherByLatLng(@Query("lat") String lat,
                                                 @Query("lon") String lon,
                                                 @Query("appid") String appid,
                                                 @Query("units") String units);
}
