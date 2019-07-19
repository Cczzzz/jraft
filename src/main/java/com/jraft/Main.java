package com.jraft;


import lombok.extern.log4j.Log4j;

import java.net.InetSocketAddress;

@Log4j
public class Main {


    public static void main(String[] args) throws InterruptedException {
        int NodeNum = 3;
        int half = NodeNum / 2 + NodeNum % 2;
        System.out.println(half);
    }
}
