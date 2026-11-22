package com.imagination.buytime.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Thread-safe wrapper around SharedPreferences for managing locked application packages
 * and securely hashing/verifying the master password with salted PBKDF2WithHmacSHA256.
 */
class LockPreferences(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val secureRandom = SecureRandom()

    companion object {
        private const val PREFS_NAME = "buytime_lock_prefs"
        private const val KEY_SALT = "key_password_salt"
        private const val KEY_HASH = "key_password_hash"
        private const val KEY_LOCKED_APPS = "key_locked_apps"

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 10_000
        private const val KEY_LENGTH = 256 // bits
        private const val SALT_LENGTH = 16 // bytes
    }

    /**
     * Checks if a master password has been configured.
     * Returns true only if both salt and hash are stored and non-empty.
     */
    @Synchronized
    fun hasPassword(): Boolean {
        val salt = prefs.getString(KEY_SALT, null)
        val hash = prefs.getString(KEY_HASH, null)
        return !salt.isNullOrEmpty() && !hash.isNullOrEmpty()
    }

    /**
     * Hashes the raw password with a fresh random 16-byte salt using PBKDF2WithHmacSHA256
     * and saves both the Base64-encoded salt and hash asynchronously via apply().
     * Plaintext passwords are never stored or exposed.
     */
    @Synchronized
    fun setPassword(rawPassword: String) {
        val saltBytes = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(saltBytes)

        val hashBytes = hashPassword(rawPassword.toCharArray(), saltBytes)

        val saltEncoded = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hashEncoded = Base64.encodeToString(hashBytes, Base64.NO_WRAP)

        prefs.edit()
            .putString(KEY_SALT, saltEncoded)
            .putString(KEY_HASH, hashEncoded)
            .apply()
    }

    /**
     * Verifies an input password against the stored PBKDF2 hash using constant-time comparison.
     * Returns false if no password is set or the input is empty.
     */
    @Synchronized
    fun verifyPassword(input: String): Boolean {
        if (!hasPassword() || input.isEmpty()) {
            return false
        }

        val saltEncoded = prefs.getString(KEY_SALT, null) ?: return false
        val hashEncoded = prefs.getString(KEY_HASH, null) ?: return false

        val saltBytes = try {
            Base64.decode(saltEncoded, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            return false
        }

        val storedHashBytes = try {
            Base64.decode(hashEncoded, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            return false
        }

        val inputHashBytes = hashPassword(input.toCharArray(), saltBytes)

        return MessageDigest.isEqual(storedHashBytes, inputHashBytes)
    }

    /**
     * Removes stored password salt and hash (useful during testing or reset).
     */
    @Synchronized
    fun clearPassword() {
        prefs.edit()
            .remove(KEY_SALT)
            .remove(KEY_HASH)
            .apply()
    }

    /**
     * Returns a defensive copy of currently locked package names.
     */
    @Synchronized
    fun getLockedPackages(): Set<String> {
        val stored = prefs.getStringSet(KEY_LOCKED_APPS, emptySet()) ?: emptySet()
        return HashSet(stored)
    }

    /**
     * Checks if a given package name is marked as locked.
     */
    @Synchronized
    fun isAppLocked(packageName: String): Boolean {
        return getLockedPackages().contains(packageName)
    }

    /**
     * Updates the lock status for a given package name.
     * Operates on and persists a defensive HashSet copy.
     */
    @Synchronized
    fun setAppLocked(packageName: String, locked: Boolean) {
        val currentLocked = HashSet(getLockedPackages())
        if (locked) {
            currentLocked.add(packageName)
        } else {
            currentLocked.remove(packageName)
        }
        prefs.edit()
            .putStringSet(KEY_LOCKED_APPS, HashSet(currentLocked))
            .apply()
    }

    private fun hashPassword(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
}
