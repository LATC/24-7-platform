package com.sindice.linker.cache;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.Future;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * TODO://refactor this class to use advance version of getting values from memcached 
 * 
 * // Try to get a value, for up to 5 seconds, and cancel if it
      // doesn't return
      Object myObj = null;
      Future<Object> f = c.asyncGet("someKey");
      try {
          myObj = f.get(5, TimeUnit.SECONDS);
      // throws expecting InterruptedException, ExecutionException
      // or TimeoutException
      } catch (Exception e) {  /*  /
          // Since we don't need this, go ahead and cancel the operation.
          // This is not strictly necessary, but it'll save some work on
          // the server.  It is okay to cancel it if running.
          f.cancel(true);
          // Do other timeout related stuff
      }
 */
public class MemcachedClientWrapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedClientWrapper.class);
	private static final char CAHCHE_KEY_REPLACE_CHAR = '_';
	private MemcachedClient mc=null;
	private boolean filterParameters = false;
	
	public MemcachedClientWrapper(MemcachedClient mc,boolean filterParameters){
		this.mc=mc;
		this.filterParameters = filterParameters;
	}
	
	public boolean isActive(){
		if(this.mc!=null){
			return true;
		}
		return false;
	}
	
	public Object get(String key) {
		return mc.get(key);
	}

	public void add(String key, int exp, Object o) {
		mc.add(key, exp, o);
	}

	public Future<Boolean> flush() {
		return mc.flush();
	}
	
    public String getCacheKey(String prefix,Map<String,String[]> map) {
        String key = prefix;
        String uniquePart ="";
        for(Map.Entry<String,String[]> entry:map.entrySet()){
    		String paramName = entry.getKey();
        	if(filterParameters){
            	if(paramName.equals("callback")||
            	   paramName.equals("_")){
            		continue;
            	}
        	}
        	for(String value:entry.getValue()){
            	uniquePart+=paramName+":"+value;
        	}
        }
        uniquePart = uniquePart.replace(' ', CAHCHE_KEY_REPLACE_CHAR)
                               .replace('\n',CAHCHE_KEY_REPLACE_CHAR)
                               .replace('\r',CAHCHE_KEY_REPLACE_CHAR);
        key+=uniquePart;
        
        if(key.length()>=128){
          try{
            byte[] bytes = key.getBytes(Charset.forName("UTF-8"));
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            BigInteger bigInt = new BigInteger(1, digest.digest(bytes));
            String md5 = bigInt.toString(16);
            key = prefix+uniquePart.substring(0,80)+md5;
          }catch(Throwable t){
            LOGGER.warn("problem making hash",t);
          }
        }
        return key;
      }
}
