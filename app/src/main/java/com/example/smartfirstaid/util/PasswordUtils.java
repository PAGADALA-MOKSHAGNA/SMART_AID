package com.example.smartfirstaid.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import android.util.Base64;

public final class PasswordUtils {
    private PasswordUtils(){}

    public static byte[] generateSalt(int size) {
        byte[] salt = new byte[size];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLenBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    public static String toBase64(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public static byte[] fromBase64(String data) {
        return Base64.decode(data, Base64.NO_WRAP);
    }
}
