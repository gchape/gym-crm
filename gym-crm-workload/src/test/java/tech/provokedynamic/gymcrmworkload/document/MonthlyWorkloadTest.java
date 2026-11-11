package tech.provokedynamic.gymcrmworkload.document;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent.ActionType;
import static org.assertj.core.api.Assertions.*;

class MonthlyWorkloadTest {
    @Test
    void cancellationCannotMakeTheTotalNegative() {
        var month = new MonthlyWorkload(1, 30);
        month.applyChange(ActionType.DELETE, 45);
        assertThat(month.getDurationMinutes()).isZero();
    }

    @Test
    void overflowDoesNotReplaceTheExistingTotalWithANegativeValue() {
        var month = new MonthlyWorkload(1, Integer.MAX_VALUE);
        assertThatThrownBy(() -> month.applyChange(ActionType.ADD, 1)).isInstanceOf(ArithmeticException.class);
        assertThat(month.getDurationMinutes()).isEqualTo(Integer.MAX_VALUE);
    }
}
