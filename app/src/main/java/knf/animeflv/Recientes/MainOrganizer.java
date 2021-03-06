package knf.animeflv.Recientes;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jordy on 17/03/2016.
 */
public class MainOrganizer {
    private static List<String> eids = new ArrayList<>();
    private static List<String> tipos = new ArrayList<>();
    private static List<String> tits = new ArrayList<>();
    private static List<MainAnimeModel> cList = new ArrayList<>();
    private static MainOrganizer organizer = new MainOrganizer();

    public static MainOrganizer init(String json) {
        if (!json.equals("null")) {
            restartLists();
            try {
                JSONArray array = new JSONObject(json).getJSONArray("lista");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    eids.add(object.getString("eid"));
                    tipos.add(object.getString("tid"));
                    tits.add(object.getString("titulo"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (organizer != null) {
                return organizer;
            } else {
                organizer = new MainOrganizer();
                return organizer;
            }
        } else {
            if (organizer != null) {
                return organizer;
            } else {
                organizer = new MainOrganizer();
                return organizer;
            }
        }
    }

    private static void restartLists() {
        eids = new ArrayList<>();
        tipos = new ArrayList<>();
        tits = new ArrayList<>();
    }

    public static List<MainAnimeModel> getList() {
        return cList;
    }

    public List<MainAnimeModel> list(Context context) {
        List<MainAnimeModel> list = new ArrayList<>();
        for (int i = 0; i < eids.size(); i++) {
            list.add(new MainAnimeModel(context, eids.get(i), tipos.get(i), tits.get(i)));
        }
        cList = list;
        return list;
    }
}
