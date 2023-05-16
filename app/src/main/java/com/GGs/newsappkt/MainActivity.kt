package com.GGs.newsappkt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.view.MenuItem
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import java.util.Locale
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(),CategoryRVAdapter.CategoryClickInterface {

    // Variables miembro
    private lateinit var rvNews: RecyclerView
    private lateinit var rvCategorias: RecyclerView
    private lateinit var pbCargando: ProgressBar
    private lateinit var categoryRVModalArrayList: ArrayList<CategoryRVModal>
    private lateinit var articlesArrayList: ArrayList<Articles>
    private lateinit var categoryRVAdapter: CategoryRVAdapter
    private lateinit var newsRVAdapter: NewsRVAdapter
    private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private var mGoogleApiClient: GoogleApiClient? = null
    private var REQUEST_LOCATION_CODE = 101
    private lateinit var geocoder: Geocoder
    private lateinit var languageExtension: String

    companion object {
        val PERMISSIONS_REQUESTED = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }




    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        languageExtension = intent.getStringExtra("languageExtension") ?: "en" // Valor predeterminado

        rvNews = findViewById(R.id.rvNews)
        rvCategorias = findViewById(R.id.rvCategorias)
        pbCargando = findViewById(R.id.pbCargando)
        articlesArrayList = ArrayList()
        categoryRVModalArrayList = ArrayList()
        newsRVAdapter = NewsRVAdapter(articlesArrayList, this)

        // Configuración del DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Listener para los elementos del NavigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Cerrar el DrawerLayout después de hacer clic en un elemento
            drawerLayout.closeDrawer(GravityCompat.START)

            // Realizar acciones según el elemento seleccionado
            when (menuItem.itemId) {
                R.id.nav_languages -> {
                    // Acción para el elemento languages
                    showLanguageSelectionDialog()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_logOut -> {
                    // Acción para el elemento log out
                    val intent = Intent(this, LogInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    return@setNavigationItemSelectedListener true
                }
                // Agregar más elementos según tus necesidades

                else -> return@setNavigationItemSelectedListener false
            }
        }


        categoryRVAdapter = CategoryRVAdapter(categoryRVModalArrayList,this,object : CategoryRVAdapter.CategoryClickInterface {
            override fun onCategoryClick(position: Int) {
                val categoria: String = categoryRVModalArrayList[position].category
                getNews(categoria)
            }
        })
        rvNews.layoutManager = LinearLayoutManager(this)
        rvNews.adapter = newsRVAdapter
        rvCategorias.adapter = categoryRVAdapter
        getCategories()
        getNews("All")
        newsRVAdapter.notifyDataSetChanged()
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .build()

        mGoogleApiClient!!.connect()
    }
    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
            .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                    })
                    .create()
                    .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    // Function to get the language extension based on the country code
    private fun getLanguageExtension(countryCode: String): String {
        return when (countryCode) {
            "ES" -> "es" // Spain
            "US" -> "en" // United States
            "FR" -> "fr" // France
            // Add more country code to language extension mappings as needed
            else -> "en" // Default to English
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getCategories(){
        categoryRVModalArrayList.add(CategoryRVModal("All","https://images.unsplash.com/photo-1504711434969-e33886168f5c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8M3x8bm90aWNpYXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Technology","https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8VGVjbm9sb2dpYXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Science","https://plus.unsplash.com/premium_photo-1676325102583-0839e57d7a1f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8N3x8Q2llbmNpYXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Sports","https://images.unsplash.com/photo-1517649763962-0c623066013b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NHx8RGVwb3J0ZXN8ZW58MHx8MHx8&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("General","https://images.unsplash.com/photo-1494059980473-813e73ee784b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NXx8R2VuZXJhbHxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Business","https://images.unsplash.com/photo-1544377193-33dcf4d68fb5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Nnx8RmluYW56YXN8ZW58MHx8MHx8&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Entertainment","https://images.unsplash.com/photo-1598899134739-24c46f58b8c0?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NHx8ZW50cmV0ZW5pbWllbnRvfGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Health","https://images.unsplash.com/photo-1532938911079-1b06ac7ceec7?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Nnx8U2FsdWR8ZW58MHx8MHx8&auto=format&fit=crop&w=400&q=60"))
        categoryRVAdapter.notifyDataSetChanged()
    }

    private fun getNews(categoria: String) {
        pbCargando.visibility = View.VISIBLE
        articlesArrayList.clear()


val categoriaURL: String = "https://newsapi.org/v2/top-headlines?language=$languageExtension&category=$categoria&sortBy=publishedAt&apikey=9b4f038f0a4c45ffa0e5f28919dfb5af"
val url: String = "https://newsapi.org/v2/top-headlines?language=$languageExtension&sortBy=publishedAt&apikey=9b4f038f0a4c45ffa0e5f28919dfb5af"



//val categoriaURL: String = "https://newsapi.org/v2/top-headlines/sources?language=en&category="+categoria+"&apiKey=03af7499e14d408ab70683b06703025e"
//val url: String = "https://newsapi.org/v2/top-headlines/sources?language=en&category=general&apiKey=03af7499e14d408ab70683b06703025e"



val url_base: String = "https:/newsapi.org/"
val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(url_base)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
val retrofitAPI: RetrofitAPI = retrofit.create(RetrofitAPI::class.java)
var call: Call<NewsModal>
if(categoria == "All"){
    call = retrofitAPI.getAllNews(url)
}else{
    call = retrofitAPI.getNewsByCategory(categoriaURL)
}
call.enqueue(object: Callback<NewsModal>{
    override fun onResponse(call: Call<NewsModal>, response: Response<NewsModal>) {
        val newsModal: NewsModal = response.body()!!
        pbCargando.visibility = View.GONE
        val articles: ArrayList<Articles> = newsModal.articles
        for (i in 0 until articles.size){
            articlesArrayList.add(Articles(articles[i].title,
            articles[i].description,articles[i].urlToImage
                ,articles[i].url,articles[i].content))
        }
        newsRVAdapter.notifyDataSetChanged()
    }

    override fun onFailure(call: Call<NewsModal>, t: Throwable) {
        Toast.makeText(this@MainActivity,"Error al obtener las noticias", Toast.LENGTH_LONG).show()
    }

})
}

override fun onCategoryClick(position: Int) {
val categoria: String = categoryRVModalArrayList[position].category
getNews(categoria)
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
    return true
}

// Realizar acciones según el elemento seleccionado en la barra de acción
when (item.itemId) {
    // Agrega más casos según tus necesidades

    android.R.id.home -> {
        // Acción para el icono del menú
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }
}

return super.onOptionsItemSelected(item)
}

private fun showLanguageSelectionDialog() {
val languages = arrayOf("English", "Spanish", "French", "Russian", "Arabic", "German") // lenguajes
val languageCodes = arrayOf("en", "es", "fr", "ru", "ar", "de") // códigos de idioma correspondientes

val builder = AlertDialog.Builder(this)
builder.setTitle("Select a Language")
    .setItems(languages) { dialog, which ->
        val selectedLanguage = languages[which]
        val languageCode = languageCodes[which] // Variable para almacenar el código de idioma según la selección
        // Realizar acciones con el lenguaje seleccionado
        Toast.makeText(this, "Selected Language: $selectedLanguage", Toast.LENGTH_SHORT).show()
        // Utilizar el languageCode para reiniciar la actividad con el nuevo lenguaje y eliminando la anterior
        when (languageCode) {
            "es" -> {
                val language = "es"
                restartMainActivity(language)
            }
            "en" -> {
                val language = "en"
                restartMainActivity(language)
            }
            "fr" -> {
                val language = "fr"
                restartMainActivity(language)
            }
            "ru" -> {
                val language = "ru"
                restartMainActivity(language)
            }
            "ar" -> {
                val language = "ar"
                restartMainActivity(language)
            }
            "de" -> {
                val language = "de"
                restartMainActivity(language)
            }
        }
    }
    .setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
    }

val dialog = builder.create()
dialog.show()
}

private fun restartMainActivity(languageExtension: String) {
val intent = Intent(this, MainActivity::class.java)
intent.putExtra("language", languageExtension)
intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
startActivity(intent)
finish()
}
}

