package com.example.smartbudgetpro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbudgetpro.databinding.ActivityRegisterBinding
import org.mindrot.jbcrypt.BCrypt

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val KEY_HASHED_PASSWORD = "hashed_password"
        private const val KEY_EMAIL = "user_email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Przycisk "Utwórz konto"
        binding.btnRegister.setOnClickListener {
            tryRegister()
        }

        // Link "Masz już konto? Zaloguj się" – wraca do ekranu logowania
        binding.tvAlreadyHaveAccount.setOnClickListener {
            finish()
        }
    }

    private fun tryRegister() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val passwordRepeat = binding.etPasswordRepeat.text.toString().trim()

        // 1. Walidacja e-mail
        if (email.isEmpty()) {
            binding.etEmail.error = "Podaj adres e-mail"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Nieprawidłowy format e-mail"
            return
        }

        // 2. Walidacja hasła
        if (password.length < 6) {
            binding.etPassword.error = "Hasło musi mieć min. 6 znaków"
            return
        }

        // 3. Powtórzone hasło
        if (password != passwordRepeat) {
            binding.etPasswordRepeat.error = "Hasła się nie zgadzają"
            return
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Jeśli konto już istnieje → informujemy i wracamy do logowania
        if (prefs.contains(KEY_HASHED_PASSWORD)) {
            Toast.makeText(this, "Konto już istnieje. Zaloguj się.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Tworzymy konto
        val hashed = BCrypt.hashpw(password, BCrypt.gensalt())
        prefs.edit()
            .putString(KEY_HASHED_PASSWORD, hashed)
            .putString(KEY_EMAIL, email)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()

        Toast.makeText(this, "Konto utworzone pomyślnie!", Toast.LENGTH_SHORT).show()

        // Przechodzimy do aplikacji
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}