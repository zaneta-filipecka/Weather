package pl.wsiz.pogoda;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
public class CityFragment extends Fragment {

    private List<String> listCities;
    private MaterialSearchBar searchBar;

    ImageView img_weather;
    TextView city_name, temperature, description, date_time, wind, pressure, humidity, sunrise, sunset, geo_coords;
    LinearLayout weather_panel;
    ProgressBar progress_bar;
    CompositeDisposable compositeDisposable;
    Open_Weather_Map_Interface mService;

    static CityFragment instance;

    public static CityFragment getInstance() {
        if(instance == null)
            instance = new CityFragment();
        return instance;
    }

    public CityFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit r = Retrofit_Client.getInstance();
        mService = r.create(Open_Weather_Map_Interface.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View itemView = inflater.inflate(R.layout.fragment_city, container, false);

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
        searchBar = (MaterialSearchBar)itemView.findViewById(R.id.searchBar);
        searchBar.setEnabled(false);

        new LoadCities().execute(); //AsyncTask class to load Cities List

        return itemView;
    }

    private class LoadCities extends SimpleAsyncTask<List<String>> {

        @Override
        protected List<String> doInBackgroundSimple() {

            listCities = new ArrayList<>();

            try{
                StringBuilder builder = new StringBuilder();
                InputStream is = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(is);

                InputStreamReader reader = new InputStreamReader(gzipInputStream);
                BufferedReader in = new BufferedReader(reader);

                String readed;

                while((readed = in.readLine()) != null){
                    builder.append(readed);
                }

                listCities = new Gson().fromJson(builder.toString(), new TypeToken<List<String>>(){}.getType());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return listCities;
        }

        @Override
        protected void onSuccess(final List<String> listCity) {
            super.onSuccess(listCity);

            searchBar.setEnabled(true);

            searchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    List<String> suggest = new ArrayList<>();

                    for(String search : listCity){
                        if(search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                            suggest.add(search);
                    }
                    searchBar.setLastSuggestions(suggest);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    getWeatherInformation(text.toString());

                    searchBar.setLastSuggestions(listCity);
                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });

            searchBar.setLastSuggestions(listCity);

            progress_bar.setVisibility(View.GONE);
        }
    }

    private void getWeatherInformation(String cityName) {
        compositeDisposable.add(mService.getWeatherByCityName(cityName,
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
                        description.setText(new StringBuilder("Weather in ").append(weatherResult.getName()).toString());
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
