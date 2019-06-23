package turnoutcom.d_sanchay.myapplication.SharedPrefManager;

import android.content.Context;
import android.content.SharedPreferences;
public class SharedPrefManager {
    private static String SHARED_PREF_NAME = "Turnout6200";
    private static SharedPrefManager mInstances;
    private  Context context;
    private static final String UID = "uid";
    private static final String NAME = "name";
    private static final String CLASSBACKUP = "classBackup";

    public SharedPrefManager(Context context){
        this.context = context;
    }
    public static synchronized SharedPrefManager getmInstances(Context context){
        if(mInstances==null){
            mInstances = new SharedPrefManager(context);
        }
        return mInstances;
    }
    public void setUser(UserId userId){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UID,userId.getUid());
        editor.putString(NAME,userId.getName());
        editor.apply();
    }
    public UserId getUser(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return new UserId(
                sharedPreferences.getString(UID,null),
                sharedPreferences.getString(NAME,null)
        );
    }
    public void LogOut(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

   public void setClassBackup(String num){
       SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
       SharedPreferences.Editor editor = sharedPreferences.edit();
       editor.putString(CLASSBACKUP,num);
       editor.apply();
   }

   public String getClassBackup(){
       SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
       return sharedPreferences.getString(CLASSBACKUP,null);
   }
}