package ru.eightbps.rxjavaautocomplete.data;

import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.eightbps.rxjavaautocomplete.data.model.PlaceAutocompleteResult;
import ru.eightbps.rxjavaautocomplete.data.model.PlaceDetailsResult;
import rx.Observable;

public interface GooglePlacesClient {
    @GET("autocomplete/json?types=(cities)")
    Observable<PlaceAutocompleteResult> autocomplete(@Query("input") String str);

    @GET("details/json")
    Observable<PlaceDetailsResult> details(@Query("placeid") String placeId);
}
