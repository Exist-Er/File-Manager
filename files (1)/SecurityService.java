package service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityService {
    private static SecurityService instance;
    private KeyPair keyPair;
    private static final String KEY_PATH = "vault/";
    private static final String PRIVATE_KEY_FILE = KEY_PATH + "private.key";
    private static final String PUBLIC_KEY_FILE  = KEY_PATH + "public.key";

    private SecurityService() {
        try {
            File vault = new File(KEY_PATH);
            if (!vault.exists()) vault.mkdirs();

            if (new File(PRIVATE_KEY_FILE).exists() && new File(PUBLIC_KEY_FILE).exists()) {
                loadKeys();
            } else {
                generateAndSaveKeys();
            }
        } catch (Exception e) {
            System.err.println("[Security] Key initialization failed: " + e.getMessage());
        }
    }

    private void loadKeys() throws Exception {
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_FILE));
        byte[] publicKeyBytes  = Files.readAllBytes(Paths.get(PUBLIC_KEY_FILE));

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(privSpec);

        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(pubSpec);

        this.keyPair = new KeyPair(publicKey, privateKey);
    }

    private void generateAndSaveKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        this.keyPair = keyGen.generateKeyPair();

        // Write public key
        Files.write(Paths.get(PUBLIC_KEY_FILE), keyPair.getPublic().getEncoded());

        // Write private key
        Files.write(Paths.get(PRIVATE_KEY_FILE), keyPair.getPrivate().getEncoded());

        // FIX #13: Restrict private key file to owner read/write only (chmod 600).
        // Without this, on a shared or multi-user system any local process can read
        // the raw private key bytes and decrypt every encrypted file in the vault.
        File privateKeyFile = new File(PRIVATE_KEY_FILE);
        privateKeyFile.setReadable(false, false);   // remove read from everyone
        privateKeyFile.setWritable(false, false);   // remove write from everyone
        privateKeyFile.setReadable(true, true);     // add read back for owner only
        privateKeyFile.setWritable(true, true);     // add write back for owner only

        System.out.println("[Security] RSA key pair generated and saved (private key: owner-only).");
    }

    public static synchronized SecurityService getInstance() {
        if (instance == null) {
            instance = new SecurityService();
        }
        return instance;
    }

    public byte[] encryptAESKey(SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        return cipher.doFinal(aesKey.getEncoded());
    }

    public SecretKey decryptAESKey(byte[] wrappedKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decodedKey = cipher.doFinal(wrappedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public byte[] encryptFile(byte[] fileData, SecretKey aesKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
        return cipher.doFinal(fileData);
    }

    public byte[] decryptFile(byte[] encryptedData, SecretKey aesKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
        return cipher.doFinal(encryptedData);
    }

    public SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public byte[] generateIV() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
