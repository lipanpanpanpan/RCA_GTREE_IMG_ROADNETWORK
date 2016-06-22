package com.cache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
* @author Weidong
*/
public class TestBean {
@SuppressWarnings("unchecked")
public static final Comparator<CacheObj> HOT_ITEM = new HotItemComparator();

public static void sortMap() {
Map<String, CacheObj> caches = new HashMap<String, CacheObj>();
CacheObj s1 = new CacheObj();
s1.setCachedCounter(2);
CacheObj s2 = new CacheObj();
s2.setCachedCounter(3);
CacheObj s3 = new CacheObj();
s3.setCachedCounter(1);
caches.put("1", s1);
caches.put("2", s2);
caches.put("3", s3);

ArrayList<CacheObj> sortList = new ArrayList<CacheObj>();
sortList.addAll(caches.values());
Collections.sort(sortList, HOT_ITEM);
Iterator<CacheObj> iter = sortList.iterator();
while (iter.hasNext()) {
CacheObj s = (CacheObj) iter.next();
System.out.println("counter is "+s.getCachedCounter());
}
}
public static void main(String[] args) {
sortMap();
}
}