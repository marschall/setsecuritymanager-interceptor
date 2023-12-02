package com.github.marschall.setsecuritymanager.interceptor.agent;

import java.util.logging.Logger;

public final class SetSecurityManagerTracer {

  public static void setSecurityManager(SecurityManager sm) {
    try {
      throw new RuntimeException("getException");
    } catch (RuntimeException e) {
      StringBuilder builder = new StringBuilder("setSecurityManager called from: \n");
      for (StackTraceElement element : e.getStackTrace()) {
        builder.append('\t')
        .append(element.getClassName())
        .append('.').append(element.getMethodName()).append("()")
        .append('\n');
      }
      Logger logger = Logger.getLogger(SetSecurityManagerTracer.class.getName());
      logger.severe(builder.toString());
    }
  }

}
