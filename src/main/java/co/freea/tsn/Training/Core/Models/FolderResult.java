package co.freea.tsn.Training.Core.Models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FolderResult extends CommonService {
    private UUID id;
    private UUID creator;
    private Timestamp created_at;
    private Timestamp edited_at;
    private String name;
    private String description;
    private Timestamp start_at;
    private Timestamp end_at;
    private FolderResult[] children;
    private FolderResult[] operations;
    ///////
    @DeepIgnore
    private String label;
    @DeepIgnore
    private String data;
    @DeepIgnore
    private String expandedIcon;
    @DeepIgnore
    private String icon;
    @DeepIgnore
    private String collapsedIcon;
    @DeepIgnore
    private String type;

    public void apply(Class<?> _class) {
        if (_class.getName().contentEquals(Folder.class.getName())) {
            this.label = name;
            this.data = name + " Folder";
            this.expandedIcon = "pi pi-folder-open";
            this.collapsedIcon = "pi pi-folder";
            this.type = "folder";
        } else if (_class.getName().contentEquals(Operation.class.getName())) {
            this.label = name;
            this.data = name;
            this.icon = "pi pi-file";
            this.type = "operation";
        } else if (_class.getName().contentEquals(Technology.class.getName())) {
            this.label = name;
            this.data = name;
            this.icon = "pi pi-file";
            this.type = "technology";
        }

    }

    @Override
    public String getTable() {
        return null;
    }
}



