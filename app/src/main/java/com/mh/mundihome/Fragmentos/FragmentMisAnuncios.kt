package com.mh.mundihome.Fragmentos

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mh.mundihome.databinding.FragmentMisAnunciosBinding

class FragmentMisAnuncios : Fragment() {

    private lateinit var binding : FragmentMisAnunciosBinding
    private lateinit var mContext : Context
    private lateinit var mTabsViewPagerAdapter : MyTabsViewPagerAdapter

    // Variable para control de seguridad RBAC
    private var isAnunciosEnabled = true

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMisAnunciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Mis anuncios"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favoritos"))

        val fragmentManager = childFragmentManager

        mTabsViewPagerAdapter = MyTabsViewPagerAdapter(fragmentManager, lifecycle)
        binding.viewPager.adapter = mTabsViewPagerAdapter

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

        // --- INICIAR CONTROL DE MÓDULOS (RBAC) ---
        verificarModuloRBAC()
    }

    /**
     * Verifica en tiempo real si el administrador ha deshabilitado esta sección.
     */
    private fun verificarModuloRBAC() {
        val refModulos = FirebaseDatabase.getInstance().getReference("Configuracion/Modulos")
        refModulos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isAnunciosEnabled = snapshot.child("anuncios_enabled").getValue(Boolean::class.java) ?: true

                    if (isAnunciosEnabled) {
                        // El módulo está activo: Mostramos el TabLayout y los sub-fragmentos
                        binding.tabLayout.visibility = View.VISIBLE
                        binding.viewPager.visibility = View.VISIBLE
                    } else {
                        // El módulo fue desactivado: Ocultamos todo por seguridad inmediata.
                        // (El MainActivity se encargará de expulsar al usuario a "Inicio")
                        binding.tabLayout.visibility = View.GONE
                        binding.viewPager.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    class MyTabsViewPagerAdapter (fragmentManager : FragmentManager, lifecycle : Lifecycle):
        FragmentStateAdapter(fragmentManager, lifecycle){
        override fun createFragment(position: Int): Fragment {
            return if (position == 0){
                Mis_Anuncios_Publicados_Fragment()
            } else {
                Fav_Anuncios_Fragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
}