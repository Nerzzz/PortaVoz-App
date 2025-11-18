package com.example.portavoz.createPost;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

import com.example.portavoz.R;

public class MapFragment extends Fragment {

    private EditText inputAddress;
    private MapView osmMap;

    private final static String TAG = "MapFragment";

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private CreatePostViewModel viewModel;

    public MapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "MapFragment carregado");
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        inputAddress = view.findViewById(R.id.inputAddress);
        osmMap = view.findViewById(R.id.osmMap);

        viewModel = new ViewModelProvider(requireActivity()).get(CreatePostViewModel.class);

        osmMap.setMultiTouchControls(true);
        osmMap.getController().setZoom(4);
        osmMap.getController().setCenter(new GeoPoint(-15.788, -47.882));

        // ENTER no campo
        inputAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {

                String text = inputAddress.getText().toString().trim();
                if (!text.isEmpty()) searchLocation(text);
                return true;
            }
            return false;
        });

        // Perde o foco
        inputAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = inputAddress.getText().toString().trim();
                if (!text.isEmpty()) searchLocation(text);
            }
        });

        return view;
    }

    public void searchLocation(String address) {

        String url = "https://nominatim.openstreetmap.org/search?format=json"
                + "&q=" + address.replace(" ", "%20")
                + "&limit=1";

        executor.execute(() -> {

            double[] coords = null;
            String returnedAddress = "";

            try {
                URL apiUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "PortavozApp/1.0");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                    throw new Exception("Erro HTTP " + conn.getResponseCode());

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONArray arr = new JSONArray(sb.toString());

                if (arr.length() > 0) {
                    JSONObject obj = arr.getJSONObject(0);
                    coords = new double[]{
                            obj.getDouble("lat"),
                            obj.getDouble("lon")
                    };
                    returnedAddress = obj.getString("display_name");
                }

            } catch (Exception e) {
                Log.e(TAG, "Erro ao buscar endereço: " + e.getMessage());
            }

            double[] finalCoords = coords;
            String finalAddress = returnedAddress;

            handler.post(() -> {
                if (!isAdded() || getContext() == null) return;

                if (finalCoords == null) {
                    Toast.makeText(getContext(), "Endereço não encontrado!", Toast.LENGTH_SHORT).show();
                    return;
                }

                loadMap(finalCoords[0], finalCoords[1], finalAddress);
            });
        });
    }

    private void loadMap(double lat, double lon, String fullAddress) {
        GeoPoint point = new GeoPoint(lat, lon);

        osmMap.getController().setCenter(point);
        osmMap.getController().setZoom(17);

        Marker marker = new Marker(osmMap);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        osmMap.getOverlays().clear();
        osmMap.getOverlays().add(marker);

        updateLocationViewModel(lat, lon, fullAddress);
    }

    private void updateLocationViewModel(double lat, double lon, String fullAddress) {
        try {
            // JSON correto para API
            JSONObject json = new JSONObject();
            json.put("latitude", lat);
            json.put("longitude", lon);

            viewModel.setLatitude(lat);
            viewModel.setLongitude(lon);
            viewModel.setAddress(fullAddress);
            viewModel.setLocationJson(json.toString());

        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar localização no ViewModel: " + e.getMessage());
        }
    }
}
