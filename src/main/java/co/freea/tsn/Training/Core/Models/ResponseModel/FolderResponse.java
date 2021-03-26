package co.freea.tsn.Training.Core.Models.ResponseModel;

import co.freea.tsn.Training.Core.Models.Folder;

public class FolderResponse {
    private String label;
    private String data;
    private String expandedIcon;
    private String collapsedIcon;
    private FolderResponse[] children;
    
    public FolderResponse(Folder folder) {
        this.label = folder.getName();
        this.data = folder.getName() + " Folder";
        this.expandedIcon = "pi pi-folder-open";
        this.collapsedIcon = "pi pi-folder";
    }


}
