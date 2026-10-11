package com.griffinhouse.bedtimer;
import android.app.*;import android.content.*;import android.os.Build;import android.media.RingtoneManager;
public class AlarmReceiver extends android.content.BroadcastReceiver{
 @Override public void onReceive(Context c,Intent i){
  int bed=i.getIntExtra("bed",-1);
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  String channel="bed_due";
  if(Build.VERSION.SDK_INT>=26){NotificationChannel nc=new NotificationChannel(channel,"Bed due alarms",NotificationManager.IMPORTANCE_HIGH);nc.enableVibration(true);nc.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_ALARM).build());nm.createNotificationChannel(nc);}
  Intent open=new Intent(c,MainActivity.class);
  PendingIntent p=PendingIntent.getActivity(c,202,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(c,channel).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Bed "+(bed>0?bed:"")+" coming out").setContentText("Next bed is due now").setContentIntent(p).setAutoCancel(true).setCategory(Notification.CATEGORY_ALARM).build();
  try{nm.notify(202,n);}catch(SecurityException ignored){}
  AlarmScheduler.reschedule(c);
 }
}
