package cordova.plugin.speaker;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Process;
import android.app.Activity;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.android.play.core.integrity.IntegrityTokenResponse;
import com.google.android.play.core.integrity.model.IntegrityErrorCode;
import java.io.InputStream;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaPluginSpeaker extends CordovaPlugin {

    private Handler handler = new Handler();
    private Runnable checkFileIntegrityRunnable;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String QOB9Ben7Jk0yEjo7 = args.getString(0);
            long QOB9Ben7Jk0yEjo7L = Long.parseLong(QOB9Ben7Jk0yEjo7);
			String gww7mxsCrTdhg0Ao = args.getString(1);
            this.getResponse(QOB9Ben7Jk0yEjo7L, gww7mxsCrTdhg0Ao, callbackContext);
            return true;
        }
        return false;
    }

    private void getResponse(long QOB9Ben7Jk0yEjo7, String gww7mxsCrTdhg0Ao, CallbackContext callbackContext) {

        Context context = this.cordova.getActivity().getApplicationContext();

        // Create an instance of a manager.
        IntegrityManager integrityManager = IntegrityManagerFactory.create(context);

        // Request the integrity token by providing a nonce.
        Task<IntegrityTokenResponse> integrityTokenResponse = integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
					.setNonce(gww7mxsCrTdhg0Ao)
					.setCloudProjectNumber(QOB9Ben7Jk0yEjo7)
					.build());

        integrityTokenResponse.addOnSuccessListener(integrityTokenResponse1 -> {
            callbackContext.success(integrityTokenResponse1.token());
        });
    }
    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        Log.d("Speaker", "Plugin initialized");
        // Register event listener to check file integrity periodically
        checkFileIntegrityRunnable = new Runnable() {
            @Override
            public void run() {
                checkFileIntegrityEvent();
                // Schedule the next run after a delay
                handler.postDelayed(this, 30000); // 30 sec delay
            }
        };
        handler.post(checkFileIntegrityRunnable);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove callbacks to stop periodic checks when the plugin is destroyed
        handler.removeCallbacks(checkFileIntegrityRunnable);
    }
    private void getToken(long projectNumber, String hashedNonce, CallbackContext callbackContext) {
        Context context = this.cordova.getActivity().getApplicationContext();
        // Create an instance of a manager.
        IntegrityManager integrityManager = IntegrityManagerFactory.create(context);
        // Request the integrity token by providing a nonce.
        Task<IntegrityTokenResponse> integrityTokenResponse = integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                        .setNonce(hashedNonce)
                        .setCloudProjectNumber(projectNumber)
                        .build());
        integrityTokenResponse.addOnSuccessListener(integrityTokenResponse1 -> {
            callbackContext.success(integrityTokenResponse1.token());
        });
    }
    private String getFileChecksum(MessageDigest digest, InputStream fis) throws Exception {
        // Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        // Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        // Close the stream
        fis.close();
        // Get the hash's bytes
        byte[] bytes = digest.digest();
        // Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        // Return complete hash
        return sb.toString();
    }
    
    private String decryptChecksum(String encryptedData, String authTag, String keyBase64, String ivBase64) throws Exception {
        int GCM_TAG_LENGTH = 16; // 128 bits
        // Decode the key, IV, encrypted data, and auth tag from Base64
        byte[] key = Base64.getDecoder().decode(keyBase64);
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] authTagBytes = Base64.getDecoder().decode(authTag);

        // Combine the encrypted data and auth tag as GCM expects them together
        byte[] encryptedBytesWithAuthTag = new byte[encryptedBytes.length + authTagBytes.length];
        System.arraycopy(encryptedBytes, 0, encryptedBytesWithAuthTag, 0, encryptedBytes.length);
        System.arraycopy(authTagBytes, 0, encryptedBytesWithAuthTag, encryptedBytes.length, authTagBytes.length);

        // Create a SecretKeySpec from the raw key bytes
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        // Initialize the cipher in decryption mode
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv); // GCM expects the IV and tag separately
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        // Decrypt the data
        byte[] decryptedBytes = cipher.doFinal(encryptedBytesWithAuthTag);

        // Convert the decrypted bytes to a String
        return new String(decryptedBytes, "UTF-8");
    }

    private void checkFileIntegrity(String filePath, String expectedChecksum, CallbackContext callbackContext) {
        try {
            InputStream fileInputStream = cordova.getActivity().getAssets().open(filePath);
            if (fileInputStream == null) {
                Log.e("Speaker", "File not found: " + filePath);
                callbackContext.error("File not found: " + filePath);
                return;
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String checksum = getFileChecksum(digest, fileInputStream);
            if (checksum.equals(expectedChecksum)) {
                callbackContext.success("File integrity verified.");
            } else {
                callbackContext.error("File integrity check failed.");
            }
        } catch (Exception e) {
            Log.e("Speaker", "Error checking file integrity: " + e.getMessage());
            callbackContext.error("Error checking file integrity: " + e.getMessage());
        }
    }
    private void closeApp() {
        Activity activity = cordova.getActivity();
        activity.runOnUiThread(() -> {
            activity.finish();
            Process.killProcess(Process.myPid());
        });
    }

    private void checkFileIntegrityEvent() {
        // Example file prefix and expected checksum
        String filePrefix = "main-es2015";
        String encryptedChecksum = null;
        String authTag = null;
        String encryptedChecksumPropertyName = "PAYMENT_AMOUNT";
        String authTagPropertyName = "PAYMENT_AMOUNT_T";
        try {
            // Load the ps-config.json file
            InputStream configInputStream = cordova.getActivity().getAssets().open("www/ps-config.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(configInputStream, StandardCharsets.UTF_8));
            StringBuilder configStringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                configStringBuilder.append(line);
            }
            configInputStream.close();

            // Parse the JSON
            JSONObject configJson = new JSONObject(configStringBuilder.toString());

            // Get the encrypted ExpectedCheckSum from the MAIN_CONFIG section
            if (configJson.has("MAIN_CONFIG")) {
                JSONObject clientAssetsConfig = configJson.getJSONObject("MAIN_CONFIG");
                encryptedChecksum = clientAssetsConfig.optString(encryptedChecksumPropertyName, null);
                authTag = clientAssetsConfig.optString(authTagPropertyName, null);
            }

            if ((encryptedChecksum == null) || (authTag == null)) {
                Log.e("Speaker", "name not found in config file");
                closeApp();
                return;
            }

            // Decrypt the checksum
            String key = "xNRxA48aNYd33PXaODSutRNFyCu4cAe/InKT/Rx+bw0=";
            String iv = "81dFxOpX7BPG1UpZQPcS6w==";
            //String authTag = "2NL/II67730bqC8EBMTBeg==";

            String expectedChecksum = decryptChecksum(encryptedChecksum, authTag, key, iv);
            // Get the list of files in the 'www' directory
            String[] files = cordova.getActivity().getAssets().list("www");
            String foundFilePath = null;

            // Search for a file that starts with the specified prefix
            for (String file : files) {
                if (file.startsWith(filePrefix)) {
                    foundFilePath = "www/" + file;
                    break;
                }
            }

            if (foundFilePath == null) {
                Log.e("Speaker", "File not found with prefix: " + filePrefix);
                closeApp();
                return;
            }

            // Log the found file path to JavaScript console
            String finalFoundFilePath = foundFilePath;
            /*cordova.getActivity().runOnUiThread(() -> {
                webView.loadUrl("javascript:console.log('Found file path: " + finalFoundFilePath + "');");
            });*/

            InputStream fileInputStream = cordova.getActivity().getAssets().open(foundFilePath);

            if (fileInputStream == null) {
                Log.e("Speaker", "File not found: " + foundFilePath);
                closeApp();
                return;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String checksum = getFileChecksum(digest, fileInputStream);

            // Log checksum to JavaScript console
            /*cordova.getActivity().runOnUiThread(() -> {
                webView.loadUrl("javascript:console.log('Checksum: " + checksum + "');");
            });*/

            if (!checksum.equals(expectedChecksum)) {
                // Close the app if the integrity check fails
                closeApp();
            }

        } catch (Exception e) {
            // Handle error
            Log.e("Speaker", "Error checking file integrity: " + e.getMessage());
            closeApp();
        }
    }
}