package co.freea.tsn.Training.Core.Models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User extends CommonService {
    @Id
    @Type(type = "org.hibernate.type.UUIDCharType")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String first_name;
    private String last_name;
    private String middle_name;
    private String address;
    private String email;
    private String phone;
    @Column(name = "seen_at")
    private Timestamp seen_at;
    private Timestamp birthday;
    @DeepType(deepLink = User.class, table = "users")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID creator;
    @Column(name = "created_at")
    private Timestamp created_at;
    @Column(name = "edited_at")
    private Timestamp edited_at;
    private String login;
    private String password;
    private boolean status;

    @Override
    @DeepIgnore
    public String getTable() {
        return "users";
    }
}



