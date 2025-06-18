package com.example.recado.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recado.R
import com.example.recado.network.Recado
import com.example.recado.network.User
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon
import android.graphics.Color

/**
 *  Map of the application. Uses OSMDROID (Open Street Map for android)
 */
@Composable
fun MapSection(context: Context, uiState: UiState, viewModel: ViewModel = viewModel()) {
    // Load osmdroid config once
    LaunchedEffect(Unit) {
        // Configuration of osmdroid
        val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        org.osmdroid.config.Configuration.getInstance().load(context, prefs)
        org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
    }
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                // The maps displayed is MAPNIK
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true
                setUseDataConnection(true)

                // Set original position and zoom
                controller.setCenter(GeoPoint(uiState.latitude, uiState.longitude))
                controller.setZoom(18.0)
            }
        },
        update = { view: MapView ->
            if(uiState.center_map == true) {
                view.controller.setCenter(GeoPoint(uiState.latitude, uiState.longitude))
                view.controller.setZoom(18.0)
                viewModel.centerMap(value = false)
            }
            // Add area and items to the view overlays
            val overlays = view.overlays
            // Clear the existing items
            overlays.clear()
            // Create the visible area
            val area = Polygon().apply {
                points = Polygon.pointsAsCircle(GeoPoint(uiState.latitude, uiState.longitude), uiState.map_radio)
                fillPaint.color = Color.argb(50, 0, 0, 255)
                outlinePaint.color = Color.BLUE
                outlinePaint.strokeWidth = 2f
                isEnabled = true
            }
            view.overlays.add(area)

            // List of items to represent in the map
            val items = ArrayList<OverlayItem>()

            if(uiState.show_recados == true) {
                for (recado: Recado in uiState.recados) {
                    items.add(
                        OverlayItem(
                            recado.recado_id,
                            "Recado",
                            null,
                            GeoPoint(recado.latitude, recado.longitude)
                        )
                    )
                }
            }
            if(uiState.show_users == true) {
                for (user: User in uiState.users) {
                    items.add(
                        OverlayItem(
                            user.user_id,
                            "Usuario",
                            user.name,
                            GeoPoint(user.latitude, user.longitude)
                        )
                    )
                }
            }
            if(uiState.show_client == true) {
                items.add(
                    OverlayItem(
                        "1",
                        "Cliente",
                        null,
                        GeoPoint(uiState.latitude, uiState.longitude)
                    )
                )
            }

            for (item in items) {
                val marker = Marker(view)
                marker.position = item.point as GeoPoint?
                val type = item.title
                marker.title = type
                // Snippet is the description of the item
                marker.snippet = item.snippet

                // Id of user or recado id
                val id = item.uid
                // Save name of user
                val name = item.snippet

                // Icons
                if(type == "Cliente") {
                    val drawable = ContextCompat.getDrawable(context, R.drawable.client)
                    marker.icon = drawable
                }
                else if(type == "Recado") {
                    if(id == uiState.selected_recado && uiState.current_dialog == DialogType.ShowRecado){
                        val drawable = ContextCompat.getDrawable(context, R.drawable.recado_selected)
                        marker.icon = drawable
                    }
                    else if(uiState.clicked_recados.contains(id)){
                        val drawable = ContextCompat.getDrawable(context, R.drawable.recado_clicked)
                        marker.icon = drawable
                    } else {
                        val drawable = ContextCompat.getDrawable(context, R.drawable.recado)
                        marker.icon = drawable
                    }
                }
                else if (type == "Usuario"){
                    if(id == uiState.selected_user && uiState.current_dialog == DialogType.Chat){
                        val drawable = ContextCompat.getDrawable(context, R.drawable.user_selected)
                        marker.icon = drawable
                    }
                    else if(uiState.clicked_users.contains(id)){
                        val drawable = ContextCompat.getDrawable(context, R.drawable.user_clicked)
                        marker.icon = drawable
                    } else {
                        val drawable = ContextCompat.getDrawable(context, R.drawable.user)
                        marker.icon = drawable
                    }
                }

                // Set up listeners, for when an icon is clicked
                marker.setOnMarkerClickListener { marker, mapView ->
                    // Display information when clicked
                    marker.showInfoWindow()

                    val type = marker.title
                    val current_dialog = uiState.current_dialog

                    // If click on client
                    if(type == "Cliente"){
                        viewModel.updateCurrentDialog(DialogType.None)
                    }
                    // If click on recado
                    else if(type == "Recado"){
                        if (current_dialog == DialogType.ShowRecado) {
                            if(id == uiState.selected_recado){
                                viewModel.updateCurrentDialog(DialogType.None)
                            }
                            else{
                                viewModel.updateSelectedRecado(id)
                            }
                        } else {
                            viewModel.updateSelectedRecado(id)
                            viewModel.updateCurrentDialog(DialogType.ShowRecado)
                        }
                    }

                    // If click on user
                    else if(type == "Usuario"){
                        if (current_dialog == DialogType.Chat) {
                            if(id == uiState.selected_user){
                                viewModel.updateCurrentDialog(DialogType.None)
                            }
                            else{
                                viewModel.getChat(id)
                                viewModel.updateSelectedUser(id, name)
                            }
                        } else {
                            viewModel.getChat(id)
                            viewModel.updateSelectedUser(id, name)
                            viewModel.updateCurrentDialog(DialogType.Chat)
                        }
                    }
                    true
                }
                overlays.add(marker)
            }
            // Force redraw
            view.invalidate()
        }
    )
}