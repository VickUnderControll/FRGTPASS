package org.Main;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.security.SecureRandom;

public class FuncionesComunes {

    // Constantes para encriptación
    private static final String SECRET_KEY = "ClaveSecreta12345"; // Cambia esto por una clave segura
    private static final String SALT = "SalDeSeguridad"; // Salt para reforzar la clave
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};:,.<>?/";
    private static final int PASSWORD_LENGTH = 16;
    public static final String backgroundNonEditable = "-fx-background-color: lightgrey;";
    public static final String backgroundEditable = "-fx-background-color: white;";

    /**
     * Método para encriptar una contraseña usando AES
     */
    public String encriptaPassword(String password) {
        try {
            // Generar clave y vector de inicialización (IV)
            SecretKey secretKey = generaClave(SECRET_KEY, SALT);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] iv = generaIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(password.getBytes());

            // Concatenar IV + Encrypted text y codificar en Base64
            byte[] encryptedWithIV = concatArrays(iv, encrypted);
            return Base64.getEncoder().encodeToString(encryptedWithIV);
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar la contraseña", e);
        }
    }

    /**
     * Método para desencriptar una contraseña encriptada
     */
    public String desencriptaPassword(String encryptedPassword) {
        try {
            // Decodificar Base64
            byte[] encryptedBytesWithIV = Base64.getDecoder().decode(encryptedPassword);

            // Extraer IV y el texto encriptado
            byte[] iv = new byte[16]; // AES utiliza bloques de 16 bytes
            byte[] encrypted = new byte[encryptedBytesWithIV.length - 16];

            System.arraycopy(encryptedBytesWithIV, 0, iv, 0, 16);
            System.arraycopy(encryptedBytesWithIV, 16, encrypted, 0, encrypted.length);

            // Configurar la clave y el vector de inicialización (IV)
            SecretKey secretKey = generaClave(SECRET_KEY, SALT);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error al desencriptar la contraseña", e);
        }
    }

    /**
     * Genera una clave AES a partir de una contraseña y un salt
     */
    private SecretKey generaClave(String password, String salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    /**
     * Genera un vector de inicialización (IV) aleatorio
     */
    private byte[] generaIV() {
        byte[] iv = new byte[16]; // AES utiliza un bloque de 16 bytes
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    /**
     * Concatena dos arreglos de bytes
     */
    private byte[] concatArrays(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }




    public static String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }



}
