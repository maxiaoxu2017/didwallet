package com.ela.wallet.sdk.didlibrary.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Utilty {

    private static Context mContext;

    private static final String SP_NAME = "didlibrary";

    private static SharedPreferences sp;

    public static boolean setPreference(@NonNull Context context, String key, String value) {
        mContext = context;
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }

        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.putString(key, value);
        return mEditor.commit();
    }

    @Nullable
    public static String getPreference(@NonNull Context context, String key, String defValue) {
        if (sp == null) {
            sp = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }

        return sp.getString(key, defValue);
    }
}
