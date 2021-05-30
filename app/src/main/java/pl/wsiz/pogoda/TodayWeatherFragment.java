package pl.wsiz.pogoda;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import pl.wsiz.pogoda.API_Pogoda.API_Pogoda;
import pl.wsiz.pogoda.Model.WeatherResult;
import pl.wsiz.pogoda.Retrofit.Open_Weather_Map_Interface;
import pl.wsiz.pogoda.Retrofit.Retrofit_Client;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayWeatherFragment extends Fragment {

    ImageView img_weather;
    TextView city_name, temperature, description, date_time, wind, pressure, humidity, sunrise, sunset, geo_coords;
    LinearLayout weather_panel;
    ProgressBar progress_bar;
    CompositeDisposable compositeDisposable;
    Open_Weather_Map_Interface mService;

    static TodayWeatherFragment instance;

    public static TodayWeatherFragment getInstance() {
        if(instance == null)
            instance = new TodayWeatherFragment();
        return instance;
    }

    public TodayWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit r = Retrofit_Client.getInstance();
        mService = r.create(Open_Weather_Map_Interface.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_today_weather, container, false);

        img_weather = (ImageView)itemView.findViewById(R.id.img_weather);
        city_name = (TextView)itemView.findViewById(R.id.city_name);
        temperature = (TextView)itemView.findViewById(R.id.temperature);
        description = (TextView)itemView.findViewById(R.id.description);
        date_time = (TextView)itemView.findViewById(R.id.date_time);
        wind = (TextView)itemView.findViewById(R.id.wind);
        pressure = (TextView)itemView.findViewById(R.id.pressure);
        humidity = (TextView)itemView.findViewById(R.id.humidity);
        sunrise = (TextView)itemView.findViewById(R.id.sunrise);
        sunset = (TextView)itemView.findViewById(R.id.sunset);
        geo_coords = (TextView)itemView.findViewById(R.id.geo_coords);
        weather_panel = (LinearLayout)itemView.findViewById(R.id.weather_panel);
        progress_bar = (ProgressBar)itemView.findViewById(R.id.progress_bar);

        getWeatherInformation();

        return itemView;
    }

    private void getWeatherInformation() {
        compositeDisposable.add(mService.getWeatherByLatLng(String.valueOf(API_Pogoda.lokalizacja.getLatitude()),
                String.valueOf(API_Pogoda.lokalizacja.getLongitude()),
                API_Pogoda.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        //Załadowanie obrazu
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                .append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(img_weather);

                        //Load information
                        city_name.setText(weatherResult.getName());
                        description.setText(new StringBuilder("Pogoda w ").append(weatherResult.getName()).toString());
                        temperature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
                        date_time.setText(API_Pogoda.convertUnixToDate(weatherResult.getDt()));
                        pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hPa").toString());
                        humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append("%").toString());
                        sunrise.setText(API_Pogoda.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        sunset.setText(API_Pogoda.convertUnixToHour(weatherResult.getSys().getSunset()));
                        geo_coords.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());

                        //Display panel
                        weather_panel.setVisibility(View.VISIBLE);
                        progress_bar.setVisibility(View.GONE);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
