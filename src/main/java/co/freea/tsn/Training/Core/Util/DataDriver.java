package co.freea.tsn.Training.Core.Util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class DataDriver {
    private final String driver;
    private final String username;
    private final String password;
}
