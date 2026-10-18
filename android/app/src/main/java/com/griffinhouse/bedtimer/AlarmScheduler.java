package com.griffinhouse.bedtimer;
import android.app.*;import android.content.*;import android.os.Build;import org.json.*;
public final class AlarmScheduler {
 static final String PREF="bed_alarm_schedule",KEY="schedule";
 static void schedule(Context c,long start,long interval,int count,int[] beds,String alarmId,String personaId,String sayText,String voiceId){
  if(interval<60000||count<1||count>30||beds.length!=count)return;
  JSONObject o=new JSONObject();try{o.put("start",start);o.put("interval",interval);o.put("count",count);JSONArray a=new JSONArray();for(int n:beds)a.put(n);o.put("beds",a);o.put("alarmId",alarmId);o.put("personaId",personaId);o.put("sayText",sayText);o.put("voiceId",voiceId);}catch(Exception e){return;}
  c.getSharedPreferences(PREF,0).edit().putString(KEY,o.toString()).apply();reschedule(c);
 }
 static void reschedule(Context c){
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  Intent intent=new Intent(c,AlarmReceiver.class);
  PendingIntent pi=PendingIntent.getBroadcast(c,101,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  am.cancel(pi);
  String raw=c.getSharedPreferences(PREF,0).getString(KEY,null);if(raw==null)return;
  try{
   JSONObject o=new JSONObject(raw);long start=o.getLong("start"),interval=o.getLong("interval");
   int count=o.getInt("count");JSONArray beds=o.getJSONArray("beds");
   if(count<1||count>30||interval<60000||beds.length()!=count)return;
   long now=System.currentTimeMillis();long index=AlarmMath.nextIndex(now,start,interval);
   long when=AlarmMath.dueAt(start,interval,index);int bed=beds.getInt(AlarmMath.bedPosition(index,count));
   intent.putExtra("bed",bed);intent.putExtra("scheduled",when);
   pi=PendingIntent.getBroadcast(c,101,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   if(Build.VERSION.SDK_INT>=31&&!am.canScheduleExactAlarms()){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);return;}
   am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
  }catch(Exception ignored){}
 }
 static void cancel(Context c){c.getSharedPreferences(PREF,0).edit().remove(KEY).apply();reschedule(c);}
}
