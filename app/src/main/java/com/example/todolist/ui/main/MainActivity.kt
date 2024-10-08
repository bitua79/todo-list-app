package com.example.todolist.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.example.todolist.R
import com.example.todolist.core.settings.AppPreferences
import com.example.todolist.core.settings.Language
import com.example.todolist.core.settings.LocaleHelper
import com.example.todolist.core.settings.Theme
import com.example.todolist.databinding.ActivityMainBinding
import com.example.todolist.ui.list.TaskListFragmentDirections
import com.example.todolist.util.collectOnActivity
import com.example.todolist.util.gone
import com.example.todolist.util.visible
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    private lateinit var switchTheme: SwitchMaterial
    private lateinit var switchLanguage: SwitchMaterial

    @Inject
    lateinit var appPreferences: AppPreferences

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val topLevelMenuItems = setOf(
        R.id.essentialTaskListFragment,
        R.id.importantTaskListFragment,
        R.id.dailyTaskListFragment,
        R.id.doneTaskListFragment,
        R.id.taskListFragment
    )

    override fun attachBaseContext(newBase: Context) {
        LocaleHelper.localizeContext(newBase, this)
        super.attachBaseContext(newBase)
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initUiComponents()
        initNavController()
        setupToolbar()
        subscribeViewModel()
        initBottomNavigation()
    }

    private fun initUiComponents() {
        switchTheme =
            binding.navigationView.menu.findItem(R.id.item_theme).actionView as SwitchMaterial
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                appPreferences.theme = Theme.DARK
            } else {
                appPreferences.theme = Theme.LIGHT
            }
        }
        switchLanguage =
            binding.navigationView.menu.findItem(R.id.item_language).actionView as SwitchMaterial

        switchLanguage.isChecked = LocaleHelper.readLanguagePref(appPreferences) == Language.Fa

        switchLanguage.setOnClickListener {
            changeLanguage()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                navController.navigate(TaskListFragmentDirections.actionToSearchFragment())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeLanguage() {
        if (LocaleHelper.readLanguagePref(appPreferences) == Language.Fa) {
            // going english
            LocaleHelper.saveLanguagePref(appPreferences, Language.En)
        } else {
            LocaleHelper.saveLanguagePref(appPreferences, Language.Fa)
        }
        LocaleHelper.localizeContext(application, application.resources.configuration)
        restartActivity()
    }

    private fun restartActivity(themeChanged: Boolean = false) {
        val intentRefresh = Intent(this, MainActivity::class.java)

        finish()
        overridePendingTransition(
            androidx.navigation.ui.R.anim.nav_default_enter_anim,
            androidx.navigation.ui.R.anim.nav_default_exit_anim
        )
        startActivity(intentRefresh)
        overridePendingTransition(
            androidx.navigation.ui.R.anim.nav_default_enter_anim,
            androidx.navigation.ui.R.anim.nav_default_exit_anim
        )
    }

    private fun initBottomNavigation() {
        val bottomNavigation = binding.bottomNavigationView

        bottomNavigation.add(
            MeowBottomNavigation.Model(
                R.id.dailyTaskListFragment,
                R.drawable.selector_coffee
            )
        )
        bottomNavigation.add(
            MeowBottomNavigation.Model(
                R.id.importantTaskListFragment,
                R.drawable.selector_time_briefcase
            )
        )
        bottomNavigation.add(
            MeowBottomNavigation.Model(
                0,
                0
            )
        )
        bottomNavigation.add(
            MeowBottomNavigation.Model(
                R.id.essentialTaskListFragment,
                R.drawable.selector_briefcase
            )
        )
        bottomNavigation.add(
            MeowBottomNavigation.Model(
                R.id.doneTaskListFragment,
                R.drawable.selector_tick_board
            )
        )
    }

    private fun initNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.taskListFragment, R.id.doneTaskListFragment, R.id.dailyTaskListFragment, R.id.importantTaskListFragment, R.id.essentialTaskListFragment -> setUiState(
                    MainPageUiState.MAIN_FRAGMENT_CONTROLS_STATE
                )

                R.id.taskFragment -> setUiState(
                    MainPageUiState.DETAIL_FRAGMENT_CONTROLS_STATE
                )
            }
        }
        binding.bottomNavigationView.setOnClickMenuListener { model ->
            navController.navigate(model.id)
        }
    }

    private fun setUiState(state: MainPageUiState) {
        when (state) {
            MainPageUiState.MAIN_FRAGMENT_CONTROLS_STATE -> {
                binding.apply {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    bottomNavigationView.visible()
                    fabAdd.visible()
                    toolbar.menu.findItem(R.id.action_search).isVisible = false
                }
            }
            MainPageUiState.DETAIL_FRAGMENT_CONTROLS_STATE -> {
                binding.apply {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    bottomNavigationView.gone()
                    fabAdd.gone()
                    toolbar.menu.findItem(R.id.action_search).isVisible = true
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))

        appBarConfiguration = AppBarConfiguration.Builder(
            topLevelMenuItems
        ).setOpenableLayout(binding.drawerLayout).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean { // setup appBarConfiguration for back arrow
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    private fun subscribeViewModel() {

        appPreferences.observeTheme().collectOnActivity(this) {
            switchTheme.isChecked = when (it) {
                Theme.DARK -> true
                Theme.LIGHT -> false
            }

            AppCompatDelegate.setDefaultNightMode(
                when (it) {
                    Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}