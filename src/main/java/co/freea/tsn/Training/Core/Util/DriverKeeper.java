package co.freea.tsn.Training.Core.Util;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum DriverKeeper {
    instance;
    private DataDriver atHome = new DataDriver("jdbc:mysql://127.0.0.1:3306/" +
            "it312mye_rsplus?useSSL=false&" +
            "allowPublicKeyRetrieval=true&" +
            "useUnicode=true&" +
            "useJDBCCompliantTimezoneShift=true&" +
            "useLegacyDatetimeCode=false&" +
            "serverTimezone=UTC", "root", "password");

    private DataDriver atWork = new DataDriver("jdbc:mysql://127.0.0.1:3306/" +
            "tsn_test?useSSL=false&" +
            "useUnicode=true&" +
            "useJDBCCompliantTimezoneShift=true&" +
            "useLegacyDatetimeCode=false&" +
            "serverTimezone=UTC", "genos", "genos15");
}
