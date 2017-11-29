package ru.eightbps.rxjavaautocomplete;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.eightbps.rxjavaautocomplete.data.RestClient;
import ru.eightbps.rxjavaautocomplete.data.model.Location;
import ru.eightbps.rxjavaautocomplete.data.model.PlaceAutocompleteResult;
import ru.eightbps.rxjavaautocomplete.data.model.PlaceDetailsResult;
import ru.eightbps.rxjavaautocomplete.data.model.Prediction;
import ru.eightbps.rxjavaautocomplete.utils.KeyboardHelper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final long DELAY_IN_MILLIS = 500;
    public static final int MIN_LENGTH_TO_START = 2;
    public static final int DEFAULT_ZOOM = 5;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autocomplete_text);
        addOnAutoCompleteTextViewItemClickedSubscriber(autoCompleteTextView);
        addOnAutoCompleteTextViewTextChangedObserver(autoCompleteTextView);

        final View clearTextButton = findViewById(R.id.clear_text_button);
        compositeDisposable.add(RxView.clicks(clearTextButton).subscribe(o -> autoCompleteTextView.setText("")));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    private void addOnAutoCompleteTextViewTextChangedObserver(final AutoCompleteTextView autoCompleteTextView) {
        Observable<PlaceAutocompleteResult> autocompleteResponseObservable =
                RxTextView.textChangeEvents(autoCompleteTextView)
                        .debounce(DELAY_IN_MILLIS, TimeUnit.MILLISECONDS)
                        .map(textViewTextChangeEvent -> textViewTextChangeEvent.text().toString())
                        .filter(s -> s.length() >= MIN_LENGTH_TO_START)
                        .observeOn(Schedulers.io())
                        .switchMap(s -> RestClient.INSTANCE.getGooglePlacesClient().autocomplete(s))
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry();

        compositeDisposable.add(
                autocompleteResponseObservable.subscribe(
                        placeAutocompleteResult -> {
                            List<NameAndPlaceId> list = new ArrayList<>();
                            for (Prediction prediction : placeAutocompleteResult.predictions) {
                                list.add(new NameAndPlaceId(prediction.description, prediction.placeId));
                            }

                            ArrayAdapter<NameAndPlaceId> itemsAdapter = new ArrayAdapter<>(MainActivity.this,
                                    android.R.layout.simple_list_item_1, list);
                            autoCompleteTextView.setAdapter(itemsAdapter);
                            String enteredText = autoCompleteTextView.getText().toString();
                            if (list.size() >= 1 && enteredText.equals(list.get(0).name)) {
                                autoCompleteTextView.dismissDropDown();
                            } else {
                                autoCompleteTextView.showDropDown();
                            }
                        },
                        e -> Log.e(TAG, "onError", e),
                        () -> Log.i(TAG, "onCompleted")));
    }

    private void addOnAutoCompleteTextViewItemClickedSubscriber(final AutoCompleteTextView autoCompleteTextView) {
        Observable<PlaceDetailsResult> adapterViewItemClickEventObservable =
                RxAutoCompleteTextView.itemClickEvents(autoCompleteTextView)
                        .map(adapterViewItemClickEvent -> {
                            NameAndPlaceId item = (NameAndPlaceId) autoCompleteTextView.getAdapter()
                                    .getItem(adapterViewItemClickEvent.position());
                            return item.placeId;
                        })
                        .observeOn(Schedulers.io())
                        .switchMap(placeId -> RestClient.INSTANCE.getGooglePlacesClient().details(placeId))
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry();

        compositeDisposable.add(
                adapterViewItemClickEventObservable.subscribe(
                        placeDetailsResult -> {
                            Log.i("PlaceAutocomplete", placeDetailsResult.toString());
                            updateMap(placeDetailsResult);
                        },
                        throwable -> Log.e(TAG, "onError", throwable),
                        () -> Log.i(TAG, "onCompleted")));
    }

    private void updateMap(PlaceDetailsResult placeDetailsResponse) {
        if (map != null) {
            map.clear();
            Location location = placeDetailsResponse.result.geometry.location;
            LatLng latLng = new LatLng(location.lat, location.lng);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placeDetailsResponse.result.name);
            Marker marker = map.addMarker(markerOptions);
            marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            KeyboardHelper.hideKeyboard(MainActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private static class NameAndPlaceId {
        final String name;
        final String placeId;

        NameAndPlaceId(String name, String placeId) {
            this.name = name;
            this.placeId = placeId;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
