package com.github.marschall.setsecuritymanager.interceptor.agent;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.not;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;

public class Agent {

  public static void premain(String arguments, Instrumentation instrumentation) {
    // make SetSecurityManagerTracer available to boot loader
    Map<TypeDescription, byte[]> classesToInject = Collections.singletonMap(new TypeDescription.ForLoadedType(SetSecurityManagerTracer.class),
        ClassFileLocator.ForClassLoader.read(SetSecurityManagerTracer.class));
    // works only on Java 8
    ClassInjector.UsingUnsafe.ofBootLoader().inject(classesToInject);
    new AgentBuilder.Default()
      // by default system classes are ignored
      .ignore(not(is(System.class)))
      // enable redefinition
      .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
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
