package br.com.mcoder.primeiroprojeto.ui

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.com.mcoder.primeiroprojeto.data.AppGraph
import br.com.mcoder.primeiroprojeto.databinding.FragmentProfileBinding
import br.com.mcoder.primeiroprojeto.util.DateTimeFormatterUtil

class ProfileFragment : Fragment() {
    interface Callbacks {
        fun onLogoutRequested()
        fun onOpenThoughtJournal()
        fun onOpenNewThought()
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogout.setOnClickListener {
            (activity as? Callbacks)?.onLogoutRequested()
        }

        binding.buttonOpenJournal.setOnClickListener {
            (activity as? Callbacks)?.onOpenThoughtJournal()
        }

        binding.buttonOpenCapture.setOnClickListener {
            (activity as? Callbacks)?.onOpenNewThought()
        }
        binding.buttonNotificationSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadProfile() {
        val user = AppGraph.authRepository.getCurrentUser() ?: return
        binding.textNameValue.text = user.name
        binding.textEmailValue.text = user.email
        binding.textMemberSinceValue.text = DateTimeFormatterUtil.formatDate(user.createdAt)
    }

    companion object {
        const val TAG = "ProfileFragment"
    }
}
