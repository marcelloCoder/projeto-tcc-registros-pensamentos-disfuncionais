package br.com.mcoder.primeiroprojeto

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import br.com.mcoder.primeiroprojeto.data.AppGraph
import br.com.mcoder.primeiroprojeto.databinding.ActivityMainBinding
import br.com.mcoder.primeiroprojeto.ui.AuthFragment
import br.com.mcoder.primeiroprojeto.ui.BottomNavigationSelectionGuard
import br.com.mcoder.primeiroprojeto.ui.ProfileFragment
import br.com.mcoder.primeiroprojeto.ui.ThoughtEditorFragment
import br.com.mcoder.primeiroprojeto.ui.ThoughtListFragment
import br.com.mcoder.primeiroprojeto.ui.ThoughtSummaryFragment
import br.com.mcoder.primeiroprojeto.notifications.AppNotifications
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.util.Locale

class MainActivity : AppCompatActivity(),
    AuthFragment.Callbacks,
    ThoughtListFragment.Callbacks,
    ThoughtEditorFragment.Callbacks,
    ProfileFragment.Callbacks,
    ThoughtSummaryFragment.Callbacks {

    private lateinit var binding: ActivityMainBinding
    private val bottomNavigationSelectionGuard = BottomNavigationSelectionGuard()
    private val notifications by lazy { AppNotifications(applicationContext) }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifications.scheduleDaily()
        if (!granted) {
            Snackbar.make(binding.root, R.string.notification_permission_denied, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration).apply {
            setLocale(Locale.forLanguageTag("pt-BR"))
        }
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.initialize(applicationContext)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentContainer.setPadding(0, systemBars.top, 0, 0)
            binding.bottomNavigation.setPadding(
                binding.bottomNavigation.paddingLeft,
                binding.bottomNavigation.paddingTop,
                binding.bottomNavigation.paddingRight,
                systemBars.bottom
            )
            insets
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (!bottomNavigationSelectionGuard.shouldHandleSelection()) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.navigation_entries -> {
                    showThoughts(clearBackStack = true)
                    true
                }

                R.id.navigation_capture -> {
                    showThoughtEditor()
                    true
                }

                R.id.navigation_profile -> {
                    showProfile(clearBackStack = true)
                    true
                }

                R.id.navigation_summary -> {
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    showRootFragment(ThoughtSummaryFragment(), ThoughtSummaryFragment.TAG)
                    true
                }

                else -> false
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            syncChrome()
        }

        if (savedInstanceState == null) {
            if (AppGraph.authRepository.getCurrentUser() == null) {
                showAuth()
            } else {
                showThoughts(clearBackStack = true)
            }
        } else {
            syncChrome()
        }
        if (AppGraph.authRepository.getCurrentUser() != null) requestNotificationPermission()
    }

    override fun onAuthSuccess() {
        showThoughts(clearBackStack = true)
        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        notifications.scheduleDaily()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(AppNotifications.EXTRA_OPEN_JOURNAL, false)) {
            if (AppGraph.authRepository.getCurrentUser() == null) showAuth()
            else showThoughts(clearBackStack = true)
        }
    }

    private fun requestNotificationPermission() {
        notifications.createChannels()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            !notifications.permissionRequested) {
            notifications.permissionRequested = true
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notifications.scheduleDaily()
        }
    }

    override fun onCreateThoughtRequested() {
        showThoughtEditor()
    }

    override fun onEditThoughtRequested(recordId: String) {
        showThoughtEditor(recordId)
    }

    override fun onThoughtSaved() {
        showThoughts(clearBackStack = true)
    }

    override fun onThoughtDeleted() {
        showThoughts(clearBackStack = true)
    }

    override fun onLogoutRequested() {
        AppGraph.authRepository.logout()
        notifications.cancelForLogout()
        showAuth()
    }

    override fun onOpenThoughtJournal() {
        showThoughts(clearBackStack = true)
    }

    override fun onOpenNewThought() {
        showThoughtEditor()
    }

    override fun onViewPeriodRequested(from: LocalDate, to: LocalDate) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        showRootFragment(ThoughtListFragment.newInstance(from, to), ThoughtListFragment.TAG)
        updateBottomNavigationSelection(R.id.navigation_entries)
    }

    private fun showAuth() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        showRootFragment(AuthFragment(), AuthFragment.TAG)
    }

    private fun showThoughts(clearBackStack: Boolean) {
        if (clearBackStack) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        showRootFragment(ThoughtListFragment(), ThoughtListFragment.TAG)
        updateBottomNavigationSelection(R.id.navigation_entries)
    }

    private fun showProfile(clearBackStack: Boolean) {
        if (clearBackStack) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        showRootFragment(ProfileFragment(), ProfileFragment.TAG)
        updateBottomNavigationSelection(R.id.navigation_profile)
    }

    private fun showThoughtEditor(recordId: String? = null) {
        val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (current is ThoughtEditorFragment && current.recordId == recordId) {
            return
        }

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                ThoughtEditorFragment.newInstance(recordId),
                ThoughtEditorFragment.TAG
            )
            .addToBackStack(ThoughtEditorFragment.TAG)
            .commit()
        syncChrome()
    }

    private fun showRootFragment(fragment: Fragment, tag: String) {
        val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (current?.javaClass == fragment.javaClass && supportFragmentManager.backStackEntryCount == 0) {
            syncChrome(current)
            return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
        syncChrome(fragment)
    }

    private fun syncChrome(fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragmentContainer)) {
        val showBottomNavigation = fragment is ThoughtListFragment || fragment is ProfileFragment ||
            fragment is ThoughtSummaryFragment
        binding.bottomNavigation.isVisible = showBottomNavigation
    }

    private fun updateBottomNavigationSelection(itemId: Int) {
        if (binding.bottomNavigation.selectedItemId == itemId) {
            return
        }

        bottomNavigationSelectionGuard.runProgrammaticUpdate {
            binding.bottomNavigation.selectedItemId = itemId
        }
    }
}
