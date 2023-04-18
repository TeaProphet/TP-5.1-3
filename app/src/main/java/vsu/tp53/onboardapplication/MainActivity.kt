package vsu.tp53.onboardapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import vsu.tp53.onboardapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        val navController = findNavController(R.id.bottomNavigationView)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.addButton.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.add_button)
                .setAction("Action", null).show()
        }
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)


//        fragmentTransaction.replace(
//            R.id.nav_host_fragment_content_main,
//            FirstFragment()
//        )
//        fragmentTransaction.commit()

        bottomNavView.setOnItemSelectedListener {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            when (it.itemId) {
                R.id.action_home -> {
                    fragmentTransaction.replace(
                        R.id.nav_host_fragment_content_main,
                        FirstFragment()
                    )
                    fragmentTransaction.commit()
                }

                R.id.action_dice -> {
                    fragmentTransaction.replace(
                        R.id.nav_host_fragment_content_main,
                        SecondFragment()
                    )
                    fragmentTransaction.commit()
                }

                R.id.action_profile -> {
                    fragmentTransaction.replace(
                        R.id.nav_host_fragment_content_main,
                        ThirdFragment()
                    )
                    fragmentTransaction.commit()
                }
            }
            true
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}