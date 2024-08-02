package com.azentio.playintegritytoken;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.android.play.core.integrity.IntegrityTokenResponse;
import com.google.android.play.core.integrity.model.IntegrityErrorCode;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaPluginSpeaker extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("integToken")) {
            String QOB9Ben7Jk0yEjo7 = args.getString(0);
            long QOB9Ben7Jk0yEjo7L = Long.parseLong(QOB9Ben7Jk0yEjo7);
			String gww7mxsCrTdhg0Ao = args.getString(1);
            this.coolMethod(QOB9Ben7Jk0yEjo7L, gww7mxsCrTdhg0Ao, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(long QOB9Ben7Jk0yEjo7, String gww7mxsCrTdhg0Ao, CallbackContext callbackContext) {

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
}
