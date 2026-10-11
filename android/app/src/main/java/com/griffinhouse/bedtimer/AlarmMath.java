package com.griffinhouse.bedtimer;
public final class AlarmMath {
 private AlarmMath(){}
 public static long nextIndex(long now,long start,long interval){
  if(interval<=0)throw new IllegalArgumentException("Interval must be positive");
  if(now<=start)return 0;
  return Math.floorDiv(now-start,interval)+1;
 }
 public static long dueAt(long start,long interval,long index){
  return Math.addExact(start,Math.multiplyExact(interval,index));
 }
 public static int bedPosition(long index,int count){
  if(count<=0)throw new IllegalArgumentException("Bed count must be positive");
  return (int)Math.floorMod(index,count);
 }
}
