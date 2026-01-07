package com.example.boost.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test {
    public static void main(String[] args) {
    System.out.println(new BCryptPasswordEncoder(12).encode("SuperAdmin@123!"));
  }
}
