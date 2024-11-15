package org.voucher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableScheduling
public class VoucherApplication {

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication.run(VoucherApplication.class, args);
        String protocol = "http";
        String localHostAddress = InetAddress.getLocalHost().getHostAddress();
        int port = 8081; // or read from configuration

        String localUrl = protocol + "://localhost:" + port;
        String externalUrl = protocol + "://" + localHostAddress + ":" + port;

        System.out.println("Local URL: " + localUrl);
        System.out.println("External URL: " + externalUrl);
    }

}
