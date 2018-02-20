package com.jarzasa.madridshops.activity

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jarzasa.madridshops.R
import com.jarzasa.madridshops.domain.interactors.getallshops.GetAllShopsInteractorImpl
import com.jarzasa.madridshops.domain.model.Shop
import com.jarzasa.madridshops.domain.model.Shops
import com.jarzasa.madridshops.fragment.ListFragment
import com.jarzasa.madridshops.router.Router
import com.jarzasa.madridshops.utils.ErrorCompletion
import com.jarzasa.madridshops.utils.SuccessCompletion

class ShopsActivity : AppCompatActivity() {

    var listFragment: ListFragment? = null
    lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shops)

        setupMap()
        setupList()

    }

    private fun setupMap() {

        val allShopsInteractor = GetAllShopsInteractorImpl(this)
        allShopsInteractor.execute(
                success = object: SuccessCompletion<Shops> {
                    override fun successCompletion(shops: Shops) {
                        initialiceMaps(shops)
                    }
                },
                error = object: ErrorCompletion {
                    override fun errorCompletion(errorMessage: String) {
                        AlertDialog.Builder(this@ShopsActivity)
                                .setTitle("ERROR")
                                .setMessage(errorMessage)
                                .setPositiveButton("Intentar", { dialog, witch ->
                                    dialog.dismiss()
                                    setupMap()
                                })
                                .setNegativeButton("Salir",  { dialog, which ->
                                    finish()
                                })
                                .show()
                    }
                }
        )
    }

    private fun setupList() {

        listFragment = supportFragmentManager.findFragmentById(R.id.activity_shops_list_fragment) as ListFragment
        val listFragmentInmutable = listFragment

        val allShopsInteractor = GetAllShopsInteractorImpl(this)
        allShopsInteractor.execute(
                success = object: SuccessCompletion<Shops> {
                    override fun successCompletion(shops: Shops) {
                        listFragmentInmutable?.setItems(shops)
                    }
                },
                error = object: ErrorCompletion {
                    override fun errorCompletion(errorMessage: String) {
                        AlertDialog.Builder(this@ShopsActivity)
                                .setTitle("ERROR")
                                .setMessage(errorMessage)
                                .setPositiveButton("Intentar", { dialog, witch ->
                                    dialog.dismiss()
                                    setupList()
                                })
                                .setNegativeButton("Salir",  { dialog, which ->
                                    finish()
                                })
                                .show()
                    }
                }
        )
    }

    private fun initialiceMaps(shops: Shops) {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.activity_shops_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync({
            //centerMapInPosition(map, 42.9017754, -2.3397827) // Zalduondo
            centerMapInPosition(it, 40.4168, -3.7038 )
            //Configuramos los settings del mapa
            it.uiSettings.isRotateGesturesEnabled = false  //No dejo girar el mapa con dos dedos)
            it.uiSettings.isZoomControlsEnabled = true     //Aparecen los controles de zoom
            //Indico mi posición
            showUserPosition(baseContext, it)
            //Actuo sobre un título de pin seleccionado
            it.setOnInfoWindowClickListener {
                val shop = shops.get(it.title)
                Log.d("Shop", shop?.name)
                if (shop != null) {
                    Router().navigateFromShopsToShopDetail(this, shop)
                }
            }
            map = it
            addAllPins(shops)

        })
    }

    fun centerMapInPosition(map: GoogleMap, latitude: Double, longitude: Double) {
        val coordinate = LatLng(latitude, longitude)
        val cameraPosition = CameraPosition.Builder()
                .target(coordinate)
                .zoom(13f)
                .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    fun addAllPins(shops: Shops) {
        shops.forEach {
            addPin(map, it.latitude.toDouble(), it.longitude.toDouble(), it.name, it.id)
        }
    }

    fun addPin(map: GoogleMap, latitude: Double, longitude: Double, title: String, id: Int) {
        map.addMarker(MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(title))
    }

    fun showUserPosition(context: Context, map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Como no tengo permisos, se los pido al usuario
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), 10)
            //Alert al usuario. Necesitamos su localización para acceder a las tiendas
            return
        }
        map.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10) {
            try {
                map.isMyLocationEnabled = true
            }
            catch (e: SecurityException) {

            }
        }
    }

}
