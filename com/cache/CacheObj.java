/*
* Created on 2003-3-5
*/
package com.cache;
/** * @author Weidong * */
public class CacheObj {
int cachedCounter;
Object cacheObj = null;
/**
* @return Returns the cacheObj.
*/
public Object getCacheObj() {
return cacheObj;
}
/**
* @param cacheObj
* The cacheObj to set.
*/
public void setCacheObj(Object cacheObj) {
this.cacheObj = cacheObj;
}
/**
* @return Returns the cachedCounter.
*/
public int getCachedCounter() {
return cachedCounter;
}
/**
* @param cachedCounter
* The cachedCounter to set.
*/
public void setCachedCounter(int cachedCounter) {
this.cachedCounter = cachedCounter;
}
}