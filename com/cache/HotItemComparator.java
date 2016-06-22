package com.cache;
import java.util.Comparator;
/**
* @author Weidong 
*/
public final class HotItemComparator implements Comparator {
/*
* @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
*/
public int compare(Object arg0, Object arg1) {
CacheObj obj1 = (CacheObj) arg0;
CacheObj obj2 = (CacheObj) arg1;
return obj1.getCachedCounter() - obj2.getCachedCounter();
}
}