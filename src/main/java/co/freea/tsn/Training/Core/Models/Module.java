package co.freea.tsn.Training.Core.Models;


import co.freea.tsn.Training.Core.Exception.ErrorMessageException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "modules")
@NoArgsConstructor
public class Module extends CommonService {
    @Id
    @Type(type = "org.hibernate.type.UUIDCharType")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String whitelist_urls;
    private String delimiter;
    @DeepType(deepLink = User.class, table = "users")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID creator;
    private String login;
    private String password;
    private boolean can_read;
    private boolean can_write;
    @Column(name = "created_at")
    private Timestamp created_at;
    @Column(name = "edited_at")
    private Timestamp edited_at;

    public boolean isValid() throws ErrorMessageException {
        String reason = "name";
        if (Optional.ofNullable(name).isPresent()) {
            reason = "creator";
            if (Optional.ofNullable(creator).isPresent()) {
                return true;
            }
        }
        throw new ErrorMessageException("Impossible to proceed with null " + reason);
    }

    @Override
    public String getTable() {
        return "modules";
    }
}
