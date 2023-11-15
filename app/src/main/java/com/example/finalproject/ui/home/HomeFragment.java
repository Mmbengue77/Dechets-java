package com.example.finalproject.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog

import com.bumptech.glide.Glide;
import com.example.finalproject.Api.ApiClient;
import com.example.finalproject.Api.ApiService;
import com.example.finalproject.R;
import com.example.finalproject.databinding.FragmentHomeBinding;
import com.example.finalproject.models.DataResponse;
import com.example.finalproject.models.Waste;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ApiService wasteReportService;
    private MapView mapView;
    private  IMapController mapController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize OSMDroid's configuration
        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // create the map
        mapView = root.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapController.setZoom(7.0);
        // Fetch data from the server and update the map
        fetchDataAndUpdateMap();








        return root;
    }

    private void fetchDataAndUpdateMap() {
        wasteReportService = ApiClient.getApiService();
        Call<DataResponse> call = wasteReportService.getWasteData();
        call.enqueue(new Callback<DataResponse>() {
            @Override
            public void onResponse(Call<DataResponse> call, Response<DataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DataResponse dataResponse = response.body();
                    List<Waste> dataList = dataResponse.getData();
                    updateMapWithMarkers(dataList);
                } else {
                    System.out.println("Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DataResponse> call, Throwable t) {
                t.printStackTrace();
                System.out.println("Retrofit onFailure: " + t.getMessage());
            }
        });
    }

    private void updateMapWithMarkers(List<Waste> dataList) {
        for (Waste data : dataList) {
            GeoPoint dataPoint = new GeoPoint(data.getLatitude(), data.getLongitude());
            mapController.setCenter(dataPoint);
            Marker dataMarker = new Marker(mapView);
            dataMarker.setPosition(dataPoint);
            dataMarker.setTitle(data.getWasteType());
            dataMarker.setSnippet(data.getWeightEstimation());
            // Set the marker click listener here
            dataMarker.setOnMarkerClickListener((marker, mapView) -> {
                // Show modal when a marker is clicked
                showMarkerDetailsModal(data);
                return false;
            });


            mapView.getOverlays().add(dataMarker);
        }
    }

    // Method to show the marker details modal
    private void showMarkerDetailsModal(Waste data) {
        // Create a custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.marker_details_dialog, null);

        // Set up the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setTitle(data.getWasteType()) // Set the title from the marker
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Load image using Glide
        ImageView imageView = dialogView.findViewById(R.id.imageView); // Replace 'R.id.imageView' with the actual ID of your ImageView
        String imageUrl = "https://6f0c-178-16-174-77.ngrok-free.app/waste/"+ data.getPhoto(); // Modify the URL accordingly
        TextView textView = dialogView.findViewById(R.id.WeightEstimation);
        textView.setText(data.getWeightEstimation()+" kg");
        Glide.with(requireContext())
                .load(imageUrl)
                .into(imageView);

        // Show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
