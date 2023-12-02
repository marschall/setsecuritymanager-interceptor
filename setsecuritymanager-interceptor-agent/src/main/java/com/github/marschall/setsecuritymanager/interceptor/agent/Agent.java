package com.github.marschall.setsecuritymanager.interceptor.agent;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.not;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.utility.JavaModule;

public class Agent {

  public static void premain(String arguments, Instrumentation instrumentation) {
//    Map<TypeDescription, byte[]> map = new HashMap<>();
    Map<TypeDescription, byte[]> classesToInject = Collections.singletonMap(new TypeDescription.ForLoadedType(SetSecurityManagerTracer.class),
        ClassFileLocator.ForClassLoader.read(SetSecurityManagerTracer.class));
//    File temp = Files.createTempDirectory("tmp").toFile();
//    ClassInjector.UsingInstrumentation.of(temp, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instrumentation).inject(map);
    ClassInjector.UsingUnsafe.ofBootLoader().inject(classesToInject);
    new AgentBuilder.Default()
//      .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
      // by default system classes are ignored
      .ignore(not(is(System.class)))
      // enable redfinition
      .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
      .with(new DebuggingListener())
      .with(new DebuggingInstallationListener())
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
  
  private static final class DebuggingListener implements AgentBuilder.Listener {

    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
      if (typeName.equals("java.lang.System")) {
        int x = 1;
      }
      // Auto-generated method stub
      
    }

    @Override
    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
        boolean loaded, DynamicType dynamicType) {
      // Auto-generated method stub
      
    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
      if (typeDescription.getTypeName().equals("java.lang.System")) {
        int x = 1;
      }
      
      
    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
        Throwable throwable) {
      // Auto-generated method stub
      
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
      // Auto-generated method stub
      
    }
    
  }
  
  private static final class DebuggingInstallationListener implements AgentBuilder.InstallationListener {

    @Override
    public void onBeforeInstall(Instrumentation instrumentation, ResettableClassFileTransformer classFileTransformer) {
      
    }

    @Override
    public void onInstall(Instrumentation instrumentation, ResettableClassFileTransformer classFileTransformer) {
      
    }

    @Override
    public Throwable onError(Instrumentation instrumentation, ResettableClassFileTransformer classFileTransformer,
        Throwable throwable) {
      return null;
    }

    @Override
    public void onReset(Instrumentation instrumentation, ResettableClassFileTransformer classFileTransformer) {
      
    }

    @Override
    public void onBeforeWarmUp(Set<Class<?>> types, ResettableClassFileTransformer classFileTransformer) {
      
    }

    @Override
    public void onWarmUpError(Class<?> type, ResettableClassFileTransformer classFileTransformer, Throwable throwable) {
      
    }

    @Override
    public void onAfterWarmUp(Map<Class<?>, byte[]> types, ResettableClassFileTransformer classFileTransformer,
        boolean transformed) {
      
    }
    
    
  }
  
  private static final class TracerInjector implements ClassFileLocator {

    @Override
    public void close() throws IOException {
      // TODO Auto-generated method stub
      
    }

    @Override
    public Resolution locate(String name) throws IOException {
      if (name.equals(SetSecurityManagerTracer.class.getName())) {
        String classFileName = SetSecurityManagerTracer.class.getName() + CLASS_FILE_EXTENSION;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = Agent.class.getClassLoader().getResourceAsStream(classFileName)) {
          byte[] buffer = new byte[1024];
          int read = inputStream.read(buffer);
          while (read != -1) {
            byteArrayOutputStream.write(buffer, 0, read);
            read = inputStream.read(buffer);
            
          }
        }
        return new Resolution.Explicit(byteArrayOutputStream.toByteArray());
      } else {
        return new Resolution.Illegal(name);
      }
    }
    
  }

  private static Method setSecurityManager() {
    try {
      return System.class.getMethod("setSecurityManager", SecurityManager.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("System#setSecurityManager not found");
    }
  }

}
