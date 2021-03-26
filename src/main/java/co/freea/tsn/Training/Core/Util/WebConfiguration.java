package co.freea.tsn.Training.Core.Util;

import org.davidmoten.rx.jdbc.ConnectionProvider;
import org.davidmoten.rx.jdbc.Database;
import org.davidmoten.rx.jdbc.pool.NonBlockingConnectionPool;
import org.davidmoten.rx.jdbc.pool.Pools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;


@Service
@Configuration
public class WebConfiguration {

    @Bean
    public Database getAsyncDatabase() {
        NonBlockingConnectionPool pool =
                Pools.nonBlocking()
                        .maxPoolSize(Runtime.getRuntime().availableProcessors() * 5)
                        .connectionProvider(ConnectionProvider.from(
                                DriverKeeper.instance.getAtHome().getDriver(),
                                DriverKeeper.instance.getAtHome().getUsername(),
                                DriverKeeper.instance.getAtHome().getPassword()
                        ))
                        .build();
        return Database.from(pool);
    }

}
