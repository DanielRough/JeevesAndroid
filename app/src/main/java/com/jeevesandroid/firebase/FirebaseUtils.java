package com.jeevesandroid.firebase;

import android.util.Base64;
import android.util.Log;

import com.jeevesandroid.AppContext;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Daniel on 24/05/2017.
 */

public class FirebaseUtils {

    //Database keys
  //  public static final String PUBLIC_KEY = "public";
   // public static final String PRIVATE_KEY = "private";
    public static final String PROJECTS_KEY = "projects";
    public static final String PATIENTS_KEY = "users";
    public static final String SURVEYDATA_KEY = "surveydata";
    //Variable types
    public static final String BOOLEAN = "Boolean";
    public static final String NUMERIC = "Numeric";
    public static final String LOCATION = "Location";
    public static final String TIME = "Time";
    public static final String DATE = "Date";
    public static final String TEXT = "Text";

    public static DatabaseReference PATIENT_REF;
    public static DatabaseReference SURVEY_REF;
    public static String STORAGE_URL;
    private static String SYMMETRICKEY;

    public static String getSymmetricKey(){
        return encodeKey(SYMMETRICKEY);
    }

    /**
     * Encrypts a user's answers to a survey using the JeevesAndroid generated
     * symmetric key
     * @param answers Semicolon-delimited String of user's survey answers
     * @return String of encrypted survey answers
     */
    public static String symmetricEncryption(String answers){
        SecretKeySpec sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("any data used as random seed".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
            SYMMETRICKEY = Base64.encodeToString(sks.getEncoded(),Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e("er", "AES secret key spec error");
        }

        // Encode the original data with AES
        byte[] encodedBytes;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(answers.getBytes());
            String base64 = Base64.encodeToString(encodedBytes,Base64.NO_WRAP);
            return base64;
        } catch (Exception e) {
            Log.e("er", "AES encryption error");
        }
        return null;
    }

    /**
     * Encrypts the symmetric key used by the Jeeves desktop environment using
     * the public key published in the project specification
     * @param symmetricKey Symmetric key generated for survey answer decryption
     * @return Encoded key or empty String if something goes wrong
     */
    public static String encodeKey(String symmetricKey){
        String pubKey = AppContext.getProject().getpubKey();
        byte[] keyBytes = Base64.decode(pubKey,Base64.DEFAULT);
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            PublicKey key = kf.generatePublic(X509publicKey);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeToString(cipher
                .doFinal(symmetricKey.getBytes(StandardCharsets.UTF_8)),Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
            BadPaddingException | IllegalBlockSizeException |
            InvalidKeyException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static FirebaseDatabase mDatabase;

        public static FirebaseDatabase getDatabase() {
            if (mDatabase == null) {
                mDatabase = FirebaseDatabase.getInstance();
                mDatabase.setPersistenceEnabled(true);
            }
            return mDatabase;

        }
}
