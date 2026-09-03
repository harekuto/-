package dev.harekuto.motifx.api.diagnostics;

import java.util.Objects;

public record Diagnostic(Severity severity, String code, String location, String message, String suggestion) {
    public enum Severity { INFO, WARNING, ERROR }

    public Diagnostic {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(message, "message");
        suggestion = suggestion == null ? "" : suggestion;
    }
}
