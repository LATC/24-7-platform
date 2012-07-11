package com.sindice.linker.provider.openid;

import java.util.Random;

public final class PasswordGenerator{

  private static Random rng = new Random();
  private static String characters = "ABCDEFGHIJKLMNOP0123456789";
  
  public static String generateString(int length){
      char[] text = new char[length];
      for (int i = 0; i < length; i++){
          text[i] = characters.charAt(rng.nextInt(characters.length()));
      }
      return new String(text);
  }
}