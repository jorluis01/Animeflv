package knf.animeflv.Explorer;

import java.io.File;

/**
 * Created by Jordy on 01/06/2016.
 */

public interface ExplorerInterfaces {
    void OnDirectoryClicked(File file, String name);

    void OnCastFile(File file, String eid);

    void OnFileClicked(File file);

    void OnDirectoryFileChange();

    void OnDirectoryEmpty(String aid);
}
