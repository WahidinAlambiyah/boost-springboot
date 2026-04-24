package com.example.boost.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DomainMetricsService {
    private final AtomicLong enrollmentTotal = new AtomicLong();
    private final AtomicLong enrollmentSuccess = new AtomicLong();
    private final AtomicLong paymentSuccess = new AtomicLong();
    private final AtomicLong paymentFailure = new AtomicLong();
    private final AtomicLong overbookedClasses = new AtomicLong();
    private final AtomicLong waitlistLength = new AtomicLong();
    private final AtomicLong notificationAttempts = new AtomicLong();
    private final AtomicLong notificationDelivered = new AtomicLong();

    private final Counter enrollmentSuccessCounter;
    private final Counter enrollmentFailureCounter;

    public DomainMetricsService(MeterRegistry meterRegistry) {
        enrollmentSuccessCounter = meterRegistry.counter("boost.enrollment.success.total");
        enrollmentFailureCounter = meterRegistry.counter("boost.enrollment.failure.total");

        Gauge.builder("boost.enrollment.success.rate", this, DomainMetricsService::getEnrollmentSuccessRate)
                .register(meterRegistry);
        Gauge.builder("boost.payment.success.total", paymentSuccess, AtomicLong::doubleValue)
                .register(meterRegistry);
        Gauge.builder("boost.payment.failure.total", paymentFailure, AtomicLong::doubleValue)
                .register(meterRegistry);
        Gauge.builder("boost.class.overbook.total", overbookedClasses, AtomicLong::doubleValue)
                .register(meterRegistry);
        Gauge.builder("boost.class.waitlist.length", waitlistLength, AtomicLong::doubleValue)
                .register(meterRegistry);
        Gauge.builder("boost.notification.delivery.rate", this, DomainMetricsService::getNotificationDeliveryRate)
                .register(meterRegistry);
    }

    public void recordEnrollment(boolean success) {
        enrollmentTotal.incrementAndGet();
        if (success) {
            enrollmentSuccess.incrementAndGet();
            enrollmentSuccessCounter.increment();
            return;
        }
        enrollmentFailureCounter.increment();
    }

    public void recordPaymentSuccess() {
        paymentSuccess.incrementAndGet();
    }

    public void recordPaymentFailure() {
        paymentFailure.incrementAndGet();
    }

    public void recordOverbook(long currentWaitlistLength) {
        overbookedClasses.incrementAndGet();
        waitlistLength.set(currentWaitlistLength);
    }

    public void updateWaitlistLength(long currentWaitlistLength) {
        waitlistLength.set(currentWaitlistLength);
    }

    public void recordNotificationDelivery(boolean delivered) {
        notificationAttempts.incrementAndGet();
        if (delivered) {
            notificationDelivered.incrementAndGet();
        }
    }

    public double getEnrollmentSuccessRate() {
        long total = enrollmentTotal.get();
        if (total == 0) {
            return 0D;
        }
        return (double) enrollmentSuccess.get() / total;
    }

    public double getNotificationDeliveryRate() {
        long total = notificationAttempts.get();
        if (total == 0) {
            return 0D;
        }
        return (double) notificationDelivered.get() / total;
    }

    public Map<String, Object> snapshot() {
        return Map.of(
                "enrollmentSuccessRate", getEnrollmentSuccessRate(),
                "payment", Map.of("success", paymentSuccess.get(), "failure", paymentFailure.get()),
                "classLoad", Map.of("overbookCount", overbookedClasses.get(), "waitlistLength", waitlistLength.get()),
                "notificationDeliveryRate", getNotificationDeliveryRate()
        );
    }
}
