package com.vibeviroma.esign.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static String USER_ID_KEY="USER_ID";
    private static String USER_INFO="USER_INFO";
    private static String LINK_AUDIO="LINK_AUDIO";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("APP_SETTINGS", 0);
    }

    public static void setUserId(String user_id, Context context) {
        SharedPreferences.Editor editor =  getSharedPreferences(context).edit();
        editor.putString(USER_ID_KEY, user_id);
        editor.commit();
    }

    public static String getUserId(Context ctx){
        return getSharedPreferences(ctx).getString(USER_ID_KEY, "");
    }

    public static void setUserInfo(String user_info, Context context) {
        SharedPreferences.Editor editor =  getSharedPreferences(context).edit();
        editor.putString(USER_INFO, user_info);
        editor.commit();
    }

    public static String getUserInfo(Context ctx){
        return getSharedPreferences(ctx).getString(USER_INFO, "");
    }


    public static void setLinkPdf(String user_info, Context context, String key) {
        SharedPreferences.Editor editor =  getSharedPreferences(context).edit();
        editor.putString(key, user_info);
        editor.commit();
    }

    public static String getLinkPdf(Context ctx, String key){
        return getSharedPreferences(ctx).getString(key, "");
    }


    public static void setLinkMedia(String user_info, Context context, String key) {
        SharedPreferences.Editor editor =  getSharedPreferences(context).edit();
        editor.putString(key+"M", user_info);
        editor.commit();
    }

    public static String getLinkMedia(Context ctx, String key){
        return getSharedPreferences(ctx).getString(key+"M", "");
    }

}
