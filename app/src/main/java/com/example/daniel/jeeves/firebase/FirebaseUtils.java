package com.example.daniel.jeeves.firebase;

import android.util.Base64;

import com.example.daniel.jeeves.ApplicationContext;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Daniel on 24/05/2017.
 */

public class FirebaseUtils {

    //Database keys
    public static String PUBLIC_KEY = "public";
    public static String PRIVATE_KEY = "private";
    public static String PROJECTS_KEY = "projects";
    public static String PATIENTS_KEY = "patients";
    public static String SURVEYS_KEY = "surveys";
    public static String SURVEYDATA_KEY = "surveydata";
    public static String SENSORDATA_KEY = "sensordata";

    //Variable types
    public static final String BOOLEAN = "Boolean";
    public static final String NUMERIC = "Numeric";
    public static final String LOCATION = "Location";
    public static final String TIME = "Time";
    public static final String DATE = "Date";
    public static final String TEXT = "Text";

    public static DatabaseReference PATIENT_REF;
    public static DatabaseReference SURVEY_REF;

    //Encryption for sensitive data
    public static String encodeAnswers(String concatAnswers){
        String pubKey = ApplicationContext.getProject().getpubKey();
        byte[] keyBytes = Base64.decode(pubKey,Base64.DEFAULT);
        //  byte[] keyBytes = Base64.decodeBase64(pubKey);
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return encryptText(concatAnswers,kf.generatePublic(X509publicKey));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String encryptText(String msg, PublicKey key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        cipher.init(Cipher.ENCRYPT_MODE, key);
        String base64 = Base64.encodeToString(cipher.doFinal(msg.getBytes("UTF-8")),Base64.NO_WRAP);
        return base64;
    }
    public static Cipher cipher;
        private static FirebaseDatabase mDatabase;

        public static FirebaseDatabase getDatabase() {
            if (mDatabase == null) {
                mDatabase = FirebaseDatabase.getInstance();
                mDatabase.setPersistenceEnabled(true);
            }
            return mDatabase;
        }
}
