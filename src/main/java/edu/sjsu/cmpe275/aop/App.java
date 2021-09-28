package edu.sjsu.cmpe275.aop;

import java.util.UUID;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
    public static void main(String[] args) {
        /***
         * Following is a dummy implementation of App to demonstrate bean creation with Application context.
         * You may make changes to suit your need, but this file is NOT part of your submission.
         */

    	ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("context.xml");
        SecretService secretService = (SecretService) ctx.getBean("secretService");
        SecretStats stats = (SecretStats) ctx.getBean("secretStats");

        try {
        	UUID secret = secretService.createSecret("Alice", "My little secret");
        	secretService.shareSecret("Alice", secret, "Bob");
        	secretService.readSecret("Bob", secret);
        	secretService.shareSecret("Alice", secret, "Bob");
        	secretService.readSecret("Bob", secret);      
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("The best known secret: " + stats.getBestKnownSecret());
        System.out.println("The worst secret keeper: " + stats.getWorstSecretKeeper());
        System.out.println("The most trusted user: " + stats.getMostTrustedUser());
        ctx.close();
    }
}
