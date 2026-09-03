package dev.harekuto.motifx.api.diagnostics;

import java.util.ArrayList;
import java.util.List;

public final class ValidationReport {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public void add(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public List<Diagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(d -> d.severity() == Diagnostic.Severity.ERROR);
    }

    public long errorCount() {
        return diagnostics.stream().filter(d -> d.severity() == Diagnostic.Severity.ERROR).count();
    }

    public long warningCount() {
        return diagnostics.stream().filter(d -> d.severity() == Diagnostic.Severity.WARNING).count();
    }
}
