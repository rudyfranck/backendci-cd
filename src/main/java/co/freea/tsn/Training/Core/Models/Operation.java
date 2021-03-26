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
@Table(name = "operations")
@NoArgsConstructor
public class Operation extends CommonService {
    @Id
    @Type(type = "org.hibernate.type.UUIDCharType")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @DeepType(deepLink = User.class, table = "users")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID creator;
    @DeepType(deepLink = Operation.class, table = "folders")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID parent;
    @Column(name = "created_at")
    private Timestamp created_at;
    @Column(name = "edited_at")
    private Timestamp edited_at;

    @Column(name = "start_at")
    private Timestamp start_at;

    @Column(name = "end_at")
    private Timestamp end_at;
    
    private String name;
    private String description;

    @DeepType(deepLink = Technology.class, table = "technologies")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id_technology;

    @Override
    @DeepIgnore
    public String getTable() {
        return "operations";
    }
}



