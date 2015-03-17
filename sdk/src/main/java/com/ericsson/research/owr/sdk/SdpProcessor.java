/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SdpProcessor {
    public static final String TAG = "SdpProcessor";
    private final ScriptableObject mScope;

    private SdpProcessor(AssetManager assetManager) throws IOException {
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_1_8);
        mScope = context.initStandardObjects();

        InputStream is = assetManager.open("sdp.js", AssetManager.ACCESS_BUFFER);
        try {
            context.evaluateReader(mScope, new InputStreamReader(is), "sdp.js", 1, null);
        } finally {
            Context.exit();
        }
    }

    public static SdpProcessor fromAssets(AssetManager assetManager) {
        try {
            return new SdpProcessor(assetManager);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject sdpToJson(String sdp) {
        if (sdp.contains("'")) {
            throw new IllegalArgumentException("SDP should not contain \"'\"");
        }
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_1_8);
        try {
            Object result = context.evaluateString(mScope, "JSON.stringify(SDP.parse('" + sdp + "'))", "<sdpToJson>", 1, null);
            try {
                return new JSONObject(result.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "failed to parse json generated from SDP: '" + result + '"');
            }
        } finally {
            Context.exit();
        }
        return null;
    }

    public String jsonToSdp(JSONObject json) {
        String jsonString = json.toString();
        if (jsonString.contains("'")) {
            throw new IllegalArgumentException("SDP json should not contain \"'\"");
        }
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_1_8);
        try {
            Object result = context.evaluateString(mScope, "SDP.generate('" + jsonString + "')", "jsonToSdp", 1, null);
            return "" + result;
        } finally {
            Context.exit();
        }
    }
}