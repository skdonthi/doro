package dev.donthi.doro.order.domain;

public class DomainException extends RuntimeException {
    private final RejectionReason reason;
    public DomainException(RejectionReason reason) {
        this.reason = reason;
    }

    public RejectionReason getReason() {
        return reason;
    }
}
