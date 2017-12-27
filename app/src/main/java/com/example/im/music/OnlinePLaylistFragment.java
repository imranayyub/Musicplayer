package com.example.im.music;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Im on 26-12-2017.
 */

public class OnlinePLaylistFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.onlineplaylist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())     //Using GSON to Convert JSON into POJO.
                .build();
        ApiInterface apiService = retrofit.create(ApiInterface.class);

        Call<List<Songinfo>> call = apiService.getDetails();   //Making Api call using Call Retrofit method that sends a request to a webserver and returns a response.
        call.enqueue(new Callback<List<Songinfo>>() {  //enqueue  send request asynchronously and notify it response or any error occurs while talking to server.
            //In case of Success and server responds.
            @Override
            public void onResponse(Call<List<Songinfo>> call, Response<List<Songinfo>> response) {
                List<Songinfo> rideHistory = response.body(); //storing response body in rideHistory.

//                for (Songinfo m : rideHistory) {
//                    Log.d("Booking_id : ", m.getName());
//                    Log.d("Image : ", m.getAlbum());
//                    Log.d("date : ", m.getArtist());
//                }
                Toast.makeText(getActivity(), "Ride History", Toast.LENGTH_SHORT).show();
            }

            //In case of Failure i.e., couldnot connect to server because of some error.
            @Override
            public void onFailure(Call<List<Songinfo>> call, Throwable t) {
                // Log error here since request failed
//                Log.e(TAG, t.toString());
                Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

