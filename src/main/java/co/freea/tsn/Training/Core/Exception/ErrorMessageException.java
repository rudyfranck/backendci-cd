package co.freea.tsn.Training.Core.Exception;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class ErrorMessageException extends Exception {
    private final String reason;

    @Override
    public String getMessage() {
        return reason;
    }

    @Override
    public synchronized Throwable getCause() {
        return new Throwable(reason);
    }
}
