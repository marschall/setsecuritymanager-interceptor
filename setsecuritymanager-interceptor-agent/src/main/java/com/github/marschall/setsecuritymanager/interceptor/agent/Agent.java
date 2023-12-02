package com.github.marschall.setsecuritymanager.interceptor.agent;

import static net.bytebuddy.matcher.ElementMatchers.is;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;


public class Agent {

  public static void premain(String arguments, Instrumentation instrumentation) {
    new AgentBuilder.Default()
      // the class is System
      .type(is(System.class))
      .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
        builder
           // the method is #setSecurityManager
          .method(is(setSecurityManager()))
          // first delegate to SetSecurityManagerTracer#setSecurityManager
          .intercept(MethodDelegation.to(SetSecurityManagerTracer.class)
              // then call the original method
              .andThen(SuperMethodCall.INSTANCE)))
      .installOn(instrumentation);
  }

  private static Method setSecurityManager() {
    try {
      return System.class.getMethod("setSecurityManager", SecurityManager.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("System#setSecurityManager not found");
    }
  }

}
