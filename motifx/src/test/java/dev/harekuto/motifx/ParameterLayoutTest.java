package dev.harekuto.motifx;

import dev.harekuto.motifx.api.graph.ParameterLayout;
import dev.harekuto.motifx.api.graph.ParameterStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ParameterLayoutTest {
    @Test
    void parameterHandleCannotBeUsedWithAnotherLayout() {
        ParameterLayout.Builder firstBuilder = ParameterLayout.builder();
        ParameterLayout.BoolParam first = firstBuilder.boolParam("enabled");
        firstBuilder.build();

        ParameterLayout.Builder secondBuilder = ParameterLayout.builder();
        secondBuilder.boolParam("other");
        ParameterStore secondStore = new ParameterStore(secondBuilder.build());

        assertThrows(IllegalArgumentException.class, () -> secondStore.set(first, true));
    }
}
