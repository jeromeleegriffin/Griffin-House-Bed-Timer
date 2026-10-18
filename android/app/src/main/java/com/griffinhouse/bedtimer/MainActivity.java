package com.griffinhouse.bedtimer;
import android.app.*;import android.os.*;import android.content.*;import android.webkit.*;import android.Manifest;import android.content.pm.PackageManager;import android.provider.Settings;import android.net.Uri;import androidx.webkit.WebViewAssetLoader;
public class MainActivity extends Activity {
 WebView web;ValueCallback<Uri[]> fileCallback;android.speech.tts.TextToSpeech voiceEngine;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  web=new WebView(this);setContentView(web);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);
  web.addJavascriptInterface(new Object(){
   @JavascriptInterface public void schedule(String json){runOnUiThread(()->{try{org.json.JSONObject o=new org.json.JSONObject(json);org.json.JSONArray a=o.getJSONArray("beds");int[] beds=new int[a.length()];for(int j=0;j<beds.length;j++)beds[j]=a.getInt(j);AlarmScheduler.schedule(MainActivity.this,o.getLong("start"),o.getLong("interval"),o.getInt("count"),beds,o.optString("alarmId","chime"),o.optString("personaId","sexy"),o.optString("sayText","Bed is coming out."),o.optString("voiceId",""));}catch(Exception ignored){}});}
   @JavascriptInterface public void listVoices(){runOnUiThread(()->{
    android.speech.tts.TextToSpeech t=new android.speech.tts.TextToSpeech(MainActivity.this,new android.speech.tts.TextToSpeech.OnInitListener(){
     @Override public void onInit(int status){
      android.speech.tts.TextToSpeech engine=voiceEngine;
      if(status!=android.speech.tts.TextToSpeech.SUCCESS||engine==null)return;
      try{org.json.JSONArray a=new org.json.JSONArray();java.util.ArrayList<android.speech.tts.Voice> list=new java.util.ArrayList<>(engine.getVoices());
       list.sort((x,y)->x.getName().compareToIgnoreCase(y.getName()));
       for(android.speech.tts.Voice v:list){if(!v.getLocale().getLanguage().equals("en"))continue;
        org.json.JSONObject item=new org.json.JSONObject();item.put("id",v.getName());item.put("label",v.getName()+" ("+v.getLocale().toLanguageTag()+")");a.put(item);}
       String encoded=org.json.JSONObject.quote(a.toString());runOnUiThread(()->web.evaluateJavascript("window.bedTimerNativeVoices("+encoded+")",null));
      }catch(Exception ignored){}
     }
    });voiceEngine=t;
   });}
   @JavascriptInterface public void previewVoice(String phrase,String persona,String voice){runOnUiThread(()->{
    Intent play=new Intent(MainActivity.this,AlarmPlaybackService.class);play.putExtra("previewVoiceOnly",true);
    play.putExtra("phrase",phrase);play.putExtra("persona",persona);play.putExtra("voiceId",voice);
    try{startForegroundService(play);}catch(Exception ignored){}
   });}
   @JavascriptInterface public void cancel(){runOnUiThread(()->AlarmScheduler.cancel(MainActivity.this));}
   @JavascriptInterface public void testAlarm(){runOnUiThread(()->{
    try { Intent play=new Intent(MainActivity.this,AlarmPlaybackService.class);play.putExtra("bed",-1);startForegroundService(play);
      android.widget.Toast.makeText(MainActivity.this,"Android alarm playback requested",android.widget.Toast.LENGTH_SHORT).show();
    } catch(Exception e){android.widget.Toast.makeText(MainActivity.this,"Alarm playback failed: "+e.getClass().getSimpleName(),android.widget.Toast.LENGTH_LONG).show();}
   });}
  },"BedNative");
  WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return loader.shouldInterceptRequest(request.getUrl());}
  });
  web.loadUrl("https://appassets.androidplatform.net/assets/bed-times.html");
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},42);
  if(Build.VERSION.SDK_INT>=31){AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);if(!am.canScheduleExactAlarms()){try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception ignored){}}}
 }
 @Override protected void onDestroy(){if(voiceEngine!=null){voiceEngine.shutdown();voiceEngine=null;}super.onDestroy();}
 @Override protected void onResume(){super.onResume();AlarmScheduler.reschedule(this);}
}
