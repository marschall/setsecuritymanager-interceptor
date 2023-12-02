package com.github.marschall.setsecuritymanager.interceptor.demo;

import org.junit.jupiter.api.Test;

class AgentTests {

  @Test
  void setSecurityManager() {
    System.setSecurityManager(new CustomSecurityManager());
  }

}
