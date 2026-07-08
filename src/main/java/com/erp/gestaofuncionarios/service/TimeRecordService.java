/*
 * MIT License
 * 
 * Copyright (c) 2026 - Jeff Oliveira
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.erp.gestaofuncionarios.service;

import com.erp.gestaofuncionarios.model.TimeRecord;
import java.time.Duration;

public class TimeRecordService {

    public static Double calculateWorkedHours(TimeRecord tr) {
        if (tr.getClockIn() == null || tr.getClockOut() == null) {
            return 0.0;
        }

        Duration totalDuration = Duration.between(tr.getClockIn(), tr.getClockOut());

        Duration lunchDuration = Duration.ZERO;
        if (tr.getLunchStart() != null && tr.getLunchEnd() != null) {
            lunchDuration = Duration.between(tr.getLunchStart(), tr.getLunchEnd());
        }

        Duration netDuration = totalDuration.minus(lunchDuration);
        if (netDuration.isNegative()) {
            return 0.0;
        }

        return netDuration.toMinutes() / 60.0;
    }
}
