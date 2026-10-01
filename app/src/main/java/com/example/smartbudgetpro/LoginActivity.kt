package com.example.smartbudgetpro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartbudgetpro.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.random.Random
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val KEY_HASHED_PASSWORD = "hashed_password"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_RESET_CODE = "temp_reset_code"
        private const val KEY_RESET_CODE_TIME = "temp_reset_code_time"

        // KONFIGURACJA SMTP – użyj konta wysyłkowego
        private const val SMTP_EMAIL = "drwalpawel20@gmail.com"
        private const val SMTP_PASSWORD = "sepk snog sbom crrl"
        private const val SMTP_HOST = "smtp.gmail.com"
        private const val SMTP_PORT = "587"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { handleLogin() }
        binding.tvAction.setOnClickListener { handleActionClick() }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasAccount = prefs.contains(KEY_HASHED_PASSWORD)

        binding.tvAction.text = if (hasAccount) "Zapomniałeś hasła? Zresetuj"
        else "Nie masz konta? Zarejestruj się"

        binding.etEmail.hint = "Podaj swój email"
        binding.btnLogin.text = "Zaloguj się"
    }

    private fun handleLogin() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasAccount = prefs.contains(KEY_HASHED_PASSWORD)

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!hasAccount) {
            Toast.makeText(this, "Nie masz konta – zarejestruj się", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Podaj email"
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Podaj hasło"
            return
        }

        val storedEmail = prefs.getString(KEY_EMAIL, "")
        val storedHash = prefs.getString(KEY_HASHED_PASSWORD, null)

        if (storedEmail == email && storedHash != null && BCrypt.checkpw(password, storedHash)) {
            prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
            startMainActivity()
        } else {
            binding.etPassword.error = "Nieprawidłowy email lub hasło"
        }
    }

    private fun handleActionClick() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasAccount = prefs.contains(KEY_HASHED_PASSWORD)

        if (hasAccount) showForgotPasswordOptions(prefs)
        else startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun showForgotPasswordOptions(prefs: android.content.SharedPreferences) {
        if (!prefs.contains(KEY_HASHED_PASSWORD)) {
            Toast.makeText(this, "Nie istnieje konto do zresetowania", Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf(
            "Wyślij kod resetu na mój e-mail",
            "Zapomniałem e-maila – całkowicie zresetuj aplikację"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Odzyskiwanie dostępu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sendResetCodeEmail(prefs)
                    1 -> showFullResetWarningDialog()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    // AUTOMATYCZNE WYSYŁANIE MAILA Z KODEM
    private fun sendResetCodeEmail(prefs: android.content.SharedPreferences) {
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        if (email.isBlank()) {
            Toast.makeText(this, "Brak zapisanego adresu e-mail", Toast.LENGTH_LONG).show()
            return
        }

        val code = Random.nextInt(100000, 999999 + 1).toString()
        prefs.edit().putString(KEY_RESET_CODE, code)
            .putLong(KEY_RESET_CODE_TIME, System.currentTimeMillis())
            .apply()

        Thread {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", SMTP_HOST)
                    put("mail.smtp.port", SMTP_PORT)
                }
                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SMTP_EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    subject = "SmartBudget Pro – Kod resetu hasła"
                    setText("Twój kod resetu: $code\nWażny 10 minut. Jeśli nie Ty – zignoruj wiadomość.")
                }

                Transport.send(message)
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Nie udało się wysłać maila", Toast.LENGTH_LONG).show()
                }
            }
        }.start()

        runOnUiThread { showEnterResetCodeDialog(prefs) }
    }

    private fun showEnterResetCodeDialog(prefs: android.content.SharedPreferences) {
        val editText = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.CENTER
            textSize = 24f
            filters = arrayOf(InputFilter.LengthFilter(6))
            hint = "Kod z e-maila"
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Weryfikacja")
            .setMessage("Wpisz 6-cyfrowy kod")
            .setView(editText)
            .setPositiveButton("Zweryfikuj") { _, _ ->
                val entered = editText.text.toString().trim()
                val saved = prefs.getString(KEY_RESET_CODE, null)
                val time = prefs.getLong(KEY_RESET_CODE_TIME, 0)

                if (saved == entered && System.currentTimeMillis() - time < 600_000) {
                    showNewPasswordDialog(prefs)
                } else {
                    Toast.makeText(this, "Nieprawidłowy lub przedawniony kod", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .setCancelable(false)
            .show()
    }

    private fun showNewPasswordDialog(prefs: android.content.SharedPreferences) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Nowe hasło (min. 6 znaków)"
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Ustaw nowe hasło")
            .setView(input)
            .setPositiveButton("Zapisz") { _, _ ->
                val newPass = input.text.toString().trim()
                if (newPass.length < 6) {
                    Toast.makeText(this, "Hasło musi mieć min. 6 znaków", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val hashed = BCrypt.hashpw(newPass, BCrypt.gensalt())
                prefs.edit().putString(KEY_HASHED_PASSWORD, hashed)
                    .remove(KEY_RESET_CODE)
                    .remove(KEY_RESET_CODE_TIME)
                    .apply()
                Toast.makeText(this, "Hasło zmienione – możesz się zalogować", Toast.LENGTH_LONG).show()
                updateUI()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showFullResetWarningDialog() {
        val message = """
            Ta operacja **trwale usunie**:
            • wszystkie wydatki
            • limity budżetowe
            • kategorie
            • zapisane hasło i adres e-mail
            
            Aplikacja uruchomi się jak po pierwszej instalacji.
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle("Całkowity reset aplikacji")
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Usuń wszystko") { _, _ -> showFinalResetConfirmationDialog() }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showFinalResetConfirmationDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            gravity = Gravity.CENTER
            hint = "Wpisz RESET"
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("OSTATNIE POTWIERDZENIE")
            .setMessage("Aby potwierdzić usunięcie wszystkich danych wpisz słowo: RESET")
            .setView(input)
            .setPositiveButton("Usuń") { _, _ ->
                if (input.text.toString().trim() == "RESET") resetAllAppData()
                else Toast.makeText(this, "Nie wpisano poprawnego słowa", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun resetAllAppData() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        try {
            val dir = filesDir.parentFile
            dir?.let { for (child in it.listFiles() ?: emptyArray()) child.deleteRecursively() }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Toast.makeText(this, "Wszystkie dane aplikacji zostały usunięte", Toast.LENGTH_LONG).show()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity()
        exitProcess(0)
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}