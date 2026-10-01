package br.com.mcoder.primeiroprojeto.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import br.com.mcoder.primeiroprojeto.R
import br.com.mcoder.primeiroprojeto.data.AppGraph
import br.com.mcoder.primeiroprojeto.databinding.FragmentAuthBinding
import com.google.android.material.snackbar.Snackbar

class AuthFragment : Fragment() {
    interface Callbacks {
        fun onAuthSuccess()
    }

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private var isLoginMode = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toggleMode.check(R.id.buttonModeLogin)
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }

            isLoginMode = checkedId == R.id.buttonModeLogin
            updateModeUi()
        }

        binding.buttonPrimary.setOnClickListener {
            if (isLoginMode) {
                handleLogin()
            } else {
                handleRegistration()
            }
        }

        binding.buttonSecondary.setOnClickListener {
            binding.toggleMode.check(
                if (isLoginMode) R.id.buttonModeRegister else R.id.buttonModeLogin
            )
        }

        updateModeUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateModeUi() {
        binding.inputLayoutName.isVisible = !isLoginMode
        binding.inputLayoutConfirmPassword.isVisible = !isLoginMode
        binding.textTitle.text = getString(
            if (isLoginMode) R.string.auth_title_sign_in else R.string.auth_title_create
        )
        binding.textBody.text = getString(
            if (isLoginMode) R.string.auth_subtitle_sign_in else R.string.auth_subtitle_create
        )
        binding.buttonPrimary.text = getString(
            if (isLoginMode) R.string.sign_in else R.string.create_account
        )
        binding.buttonSecondary.text = getString(
            if (isLoginMode) R.string.need_account else R.string.have_account
        )
    }

    private fun handleLogin() {
        val result = AppGraph.authRepository.login(
            email = binding.inputEmail.text?.toString().orEmpty(),
            password = binding.inputPassword.text?.toString().orEmpty()
        )
        handleAuthResult(result)
    }

    private fun handleRegistration() {
        val password = binding.inputPassword.text?.toString().orEmpty()
        val confirmPassword = binding.inputConfirmPassword.text?.toString().orEmpty()

        binding.inputLayoutConfirmPassword.error = null
        if (password != confirmPassword) {
            binding.inputLayoutConfirmPassword.error = getString(R.string.error_password_match)
            return
        }

        val result = AppGraph.authRepository.register(
            name = binding.inputName.text?.toString().orEmpty(),
            email = binding.inputEmail.text?.toString().orEmpty(),
            password = password
        )
        handleAuthResult(result)
    }

    private fun handleAuthResult(result: Result<*>) {
        result.onSuccess {
            (activity as? Callbacks)?.onAuthSuccess()
        }.onFailure { error ->
            Snackbar.make(binding.root, error.message.orEmpty(), Snackbar.LENGTH_LONG).show()
        }
    }

    companion object {
        const val TAG = "AuthFragment"
    }
}
