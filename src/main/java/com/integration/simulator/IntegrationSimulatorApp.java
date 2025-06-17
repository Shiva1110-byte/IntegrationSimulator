package com.integration.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableAsync
@RestController
@RequestMapping("/simulate")
public class IntegrationSimulatorApp {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationSimulatorApp.class);

    public static void main(String[] args) {
        SpringApplication.run(IntegrationSimulatorApp.class, args);
    }

    @PostMapping("/salesforce")
    public ResponseEntity<Map<String, Object>> simulateSalesforce(@Valid @RequestBody Map<String, Object> payload) {
        logger.info("[Salesforce] Payload: {}", payload);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("salesforce_id", "SF-" + new Random().nextInt(9000));
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stripe")
    public ResponseEntity<Map<String, Object>> simulateStripe(@Valid @RequestBody Map<String, Object> payload) {
        logger.info("[Stripe] Payload: {}", payload);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "charged");
        response.put("charge_id", "ch_" + new Random().nextInt(999999));
        response.put("processed_at", new Date());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/servicenow")
    public ResponseEntity<Map<String, Object>> simulateServiceNow(@Valid @RequestBody Map<String, Object> payload) {
        logger.info("[ServiceNow] Payload: {}", payload);
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("number", "INC" + new Random().nextInt(99999));
        ticket.put("status", "opened");
        ticket.put("opened_at", new Date());
        return ResponseEntity.ok(Map.of("ticket", ticket));
    }

    @PostMapping("/retry-logic")
    public ResponseEntity<Map<String, Object>> retryLogic() throws InterruptedException {
        logger.info("[RetryLogic] Starting retry simulation...");
        int attempts = 0;
        Random rand = new Random();
        while (attempts < 3) {
            if (rand.nextDouble() < 0.7) {
                attempts++;
                Thread.sleep(1000);
                logger.warn("Attempt {} failed, retrying...", attempts);
            } else {
                logger.info("Attempt {} succeeded", attempts + 1);
                return ResponseEntity.ok(Map.of("status", "success", "attempt", attempts + 1));
            }
        }
        logger.error("All attempts failed");
        return ResponseEntity.ok(Map.of("status", "failed", "attempt", attempts));
    }

    @Async
    @PostMapping("/async/process")
    public CompletableFuture<ResponseEntity<String>> asyncProcess(@RequestBody @NotNull Map<String, Object> payload) {
        logger.info("[Async] Received for processing: {}", payload);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
                logger.info("[Async] Completed processing.");
                return ResponseEntity.ok("Async process complete");
            } catch (InterruptedException e) {
                logger.error("[Async] Error during async processing", e);
                return ResponseEntity.internalServerError().body("Processing interrupted");
            }
        });
    }
}
