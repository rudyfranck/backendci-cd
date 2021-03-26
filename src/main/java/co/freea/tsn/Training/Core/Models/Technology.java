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
@Table(name = "technologies")
@NoArgsConstructor
public class Technology extends CommonService {
    @Id
    @Type(type = "org.hibernate.type.UUIDCharType")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @DeepType(deepLink = User.class, table = "users")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID creator;
    @DeepType(deepLink = Folder.class, table = "folders")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID parent;
    @Column(name = "created_at")
    private Timestamp created_at;
    @Column(name = "edited_at")
    private Timestamp edited_at;
    private String name;
    private String description;

    @Override
    @DeepIgnore
    public String getTable() {
        return "technologies";
    }
}



