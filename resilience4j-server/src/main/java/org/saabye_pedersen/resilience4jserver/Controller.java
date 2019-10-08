package org.saabye_pedersen.resilience4jserver;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Controller {

    private int slowCounterTime = 1;
    private int slowErrorCounter = 1;

    @GetMapping(path = "/time")
    public String getTime() {
        return "{\"time\": \"" + LocalDateTime.now().toString() + "\"}";
    }

    @GetMapping(path = "/slowTime")
    public String getSlowTime() throws InterruptedException {

        System.out.printf("Slow counter is %d\n", slowCounterTime);

        if ((slowCounterTime++ % 10) != 0) {
            System.out.print("\tWill sleep\n");
            Thread.sleep(30_000);
        }

        System.out.print("\tReturning time\n");
        return "{\"time\": \"" + LocalDateTime.now().toString() + "\"}";
    }

    @GetMapping(path = "/slowFaultyTime")
    public ResponseEntity<String> getSlowErrorResponseTime() {

        System.out.printf("Faulty counter is %d\n", slowErrorCounter);
        if ((slowErrorCounter++ % 10) != 0) {
            System.out.print("\tReturning Server Error\n");
            return new ResponseEntity<>(
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        System.out.print("\tReturning OK\n");
        return new ResponseEntity<>(
                "{\"time\": \"" + LocalDateTime.now().toString() + "\"}", HttpStatus.OK
        );
    }

}