package com.mh.mundihome.Admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.mh.mundihome.R
import com.mh.mundihome.OpcionesLogin

class AdminMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Módulo inicial por defecto
        if (savedInstanceState == null) {
            reemplazarFragment(Dashboard())
            navigationView.setCheckedItem(R.id.nav_dashboard)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> reemplazarFragment(Dashboard())
            R.id.nav_usuarios -> reemplazarFragment(GestionUsuarios())
            R.id.nav_anuncios -> reemplazarFragment(ModeracionAnuncios())
            R.id.nav_resenas -> reemplazarFragment(ResenasReportes())
            R.id.nav_modulos -> reemplazarFragment(ControlModulos()) // Aquí va el RBAC
            R.id.nav_perfil -> reemplazarFragment(PerfilAdmin())
            R.id.nav_salir -> cerrarSesion()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun reemplazarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, OpcionesLogin::class.java))
        finish()
    }
}

