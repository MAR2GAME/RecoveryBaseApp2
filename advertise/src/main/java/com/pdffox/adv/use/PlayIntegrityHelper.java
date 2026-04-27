package com.pdffox.adv.use;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.StandardIntegrityManager;
import com.pdffox.adv.AdvApplicaiton;import com.pdffox.adv.adv.AdvCheckManager;
import com.pdffox.adv.util.PreferenceUtil;

import java.security.MessageDigest;

public class PlayIntegrityHelper {
    private static final String TAG = "PlayIntegrityHelper";
    StandardIntegrityManager.StandardIntegrityTokenProvider integrityTokenProvider;
    long cloudProjectNumber = 804850522653L;

    public void requestPlayIntegrity() {

        Log.e(TAG, "requestPlayIntegrity: " );
        StandardIntegrityManager standardIntegrityManager = IntegrityManagerFactory.createStandard(AdvApplicaiton.Companion.getInstance());
        StandardIntegrityManager.PrepareIntegrityTokenRequest request = StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(cloudProjectNumber)
                .build();
        standardIntegrityManager.prepareIntegrityToken(request)
                .addOnSuccessListener(tokenProvider -> {
                    integrityTokenProvider = tokenProvider;
                    Log.e(TAG, "requestPlayIntegrity: has requested tokenProvider" );

                    String input = System.currentTimeMillis() + "-" + Math.random();
                    String requestHash = generateRequestHash(input);
                    PreferenceUtil.INSTANCE.commitString("requestHash", requestHash);
                    if (requestHash == null) {
                        Log.e(TAG, "requestHash 生成失败");
                        return;
                    }
                    Task<StandardIntegrityManager.StandardIntegrityToken> integrityTokenResponse =
                            integrityTokenProvider.request(
                                    StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                            .setRequestHash(requestHash)
                                            .build());
                    integrityTokenResponse
                            .addOnSuccessListener(response -> sendToServer(response.token()))
                            .addOnFailureListener(this::handleError);
                })
                .addOnFailureListener(this::handleError);
    }

    private String generateRequestHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.e(TAG, "generateRequestHash error", e);
            return null;
        }
    }

    private void sendToServer(String token) {
        Log.e(TAG, "sendToServer: token = " + token);
        // 切换到子线程执行
        new Thread(() -> AdvCheckManager.INSTANCE.checkToken(token)).start();
    }

    private void handleError(Exception exception) {
        Log.e(TAG, "handleError: ", exception);
    }

}
