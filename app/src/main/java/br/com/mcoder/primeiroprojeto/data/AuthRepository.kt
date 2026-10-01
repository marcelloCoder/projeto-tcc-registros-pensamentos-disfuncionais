package br.com.mcoder.primeiroprojeto.data

import br.com.mcoder.primeiroprojeto.model.UserAccount
import br.com.mcoder.primeiroprojeto.util.IdGenerator
import br.com.mcoder.primeiroprojeto.util.InputValidators
import br.com.mcoder.primeiroprojeto.util.PasswordHasher
import java.util.Locale

class AuthRepository(private val storage: AppStorage) {
    fun register(name: String, email: String, password: String): Result<UserAccount> {
        val cleanName = name.trim()
        val normalizedEmail = email.trim().lowercase(Locale.US)

        if (cleanName.isBlank()) {
            return failure("Informe seu nome.")
        }
        if (!InputValidators.isValidEmail(normalizedEmail)) {
            return failure("Informe um e-mail válido.")
        }
        if (password.length < 6) {
            return failure("A senha deve ter pelo menos 6 caracteres.")
        }

        val users = storage.loadUsers().toMutableList()
        if (users.any { it.email == normalizedEmail }) {
            return failure("Já existe uma conta com este e-mail.")
        }

        val user = UserAccount(
            id = IdGenerator.next("user"),
            name = cleanName,
            email = normalizedEmail,
            passwordHash = PasswordHasher.hash(password),
            createdAt = System.currentTimeMillis()
        )

        users += user
        storage.saveUsers(users)
        storage.saveSessionUserId(user.id)
        return Result.success(user)
    }

    fun login(email: String, password: String): Result<UserAccount> {
        val normalizedEmail = email.trim().lowercase(Locale.US)
        val user = storage.loadUsers().firstOrNull { it.email == normalizedEmail }
            ?: return failure("Nenhuma conta foi encontrada com este e-mail.")

        if (user.passwordHash != PasswordHasher.hash(password)) {
            return failure("Senha incorreta.")
        }

        storage.saveSessionUserId(user.id)
        return Result.success(user)
    }

    fun getCurrentUser(): UserAccount? {
        val userId = storage.loadSessionUserId() ?: return null
        return storage.loadUsers().firstOrNull { it.id == userId }
    }

    fun logout() {
        storage.saveSessionUserId(null)
    }

    private fun <T> failure(message: String): Result<T> {
        return Result.failure(IllegalArgumentException(message))
    }
}
