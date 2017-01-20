package knf.animeflv.AutoEmision;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import knf.animeflv.ColorsRes;
import knf.animeflv.R;
import knf.animeflv.Recientes.MainAnimeModel;
import knf.animeflv.Seen.SeenManager;
import knf.animeflv.Utils.CacheManager;
import knf.animeflv.Utils.ExecutorManager;
import knf.animeflv.Utils.ThemeUtils;
import knf.animeflv.info.Helper.InfoHelper;

public class AutoEmisionAdapter extends RecyclerView.Adapter<AutoEmisionAdapter.ViewHolder> implements DraggableItemAdapter<AutoEmisionAdapter.ViewHolder> {
    private Activity context;
    private List<EmObj> list;
    private int daycode;
    private OnListInteraction interaction;
    private boolean isUpdating = false;

    public AutoEmisionAdapter(Activity context, List<EmObj> list, int daycode, OnListInteraction interaction) {
        this.context = context;
        this.daycode = daycode;
        this.list = list;
        this.interaction = interaction;
        setHasStableIds(true);
    }

    @Override
    public AutoEmisionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.item_anime_em, parent, false);
        return new AutoEmisionAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AutoEmisionAdapter.ViewHolder holder, final int position) {
        new CacheManager().mini(context, list.get(position).getAid(), holder.imageView);
        if (ThemeUtils.isAmoled(context)) {
            holder.title.setTextColor(ColorsRes.Blanco(context));
            holder.cardView.setCardBackgroundColor(ColorsRes.Prim(context));
        } else {
            holder.title.setTextColor(Color.parseColor("#4d4d4d"));
            holder.cardView.setCardBackgroundColor(ColorsRes.Blanco(context));
        }
        holder.state.setColorFilter(ThemeUtils.getAcentColor(context));
        setIconbyState(getState(list.get(position).getAid()), holder.state);
        holder.title.setText(list.get(position).getTitle());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoHelper.open(
                        context,
                        new InfoHelper.SharedItem(holder.imageView, "img"),
                        new InfoHelper.BundleItem("aid", list.get(holder.getAdapterPosition()).getAid()),
                        new InfoHelper.BundleItem("title", list.get(holder.getAdapterPosition()).getTitle())
                );
            }
        });
        final int dragState = holder.getDragStateFlags();
        if (((dragState & Draggable.STATE_FLAG_IS_UPDATED) != 0)) {
            if ((dragState & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
                holder.cardView.setCardBackgroundColor(ThemeUtils.getAcentColor(context));
                holder.state.setColorFilter(ThemeUtils.isAmoled(context) ? ColorsRes.Prim(context) : ColorsRes.Blanco(context));
                holder.title.setTextColor(ColorsRes.Blanco(context));
            } else {
                holder.cardView.setCardBackgroundColor(ThemeUtils.isAmoled(context) ? ColorsRes.Prim(context) : ColorsRes.Blanco(context));
            }
        }
    }

    public void setIconbyState(StateType type, final ImageView imageView) {
        switch (type) {
            case SEEN:
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageResource(R.drawable.d_seen);
                        imageView.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case OUT:
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageResource(R.drawable.seen);
                        imageView.setVisibility(View.VISIBLE);
                    }
                });
                break;
            default:
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setVisibility(View.GONE);
                    }
                });
                break;
        }
    }

    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        return !isUpdating;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(final int fromPosition, final int toPosition) {
        if (fromPosition != toPosition) {
            EmObj tmp = list.get(fromPosition);
            list.remove(list.get(fromPosition));
            list.add(toPosition, tmp);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyItemMoved(fromPosition, toPosition);
                }
            });
            interaction.onListEdited(list);
        }
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public StateType getState(String aid) {
        if (daycode == getActualDayCode()) {
            List<MainAnimeModel> models = AutoEmisionListHolder.getEpisodes();
            for (MainAnimeModel model : models) {
                if (model.getEid().contains(aid)) {
                    if (SeenManager.get(context).isSeen(model.getEid())) {
                        return StateType.SEEN;
                    } else {
                        return StateType.OUT;
                    }
                }
            }
        }
        return StateType.NONE;
    }

    public void updatelist() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                isUpdating = true;
                list = AutoEmisionListHolder.getList(daycode);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
                isUpdating = false;
                return null;
            }
        }.executeOnExecutor(ExecutorManager.getExecutor());
    }

    private int getActualDayCode() {
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
            case Calendar.SUNDAY:
                return 7;
            default:
                return 1;
        }
    }


    private enum StateType {
        NONE(0),
        SEEN(1),
        OUT(2);
        int value;

        StateType(int value) {
            this.value = value;
        }
    }

    private interface Draggable extends DraggableItemConstants {
    }

    public static class ViewHolder extends AbstractDraggableItemViewHolder {
        @BindView(R.id.cardRel)
        CardView cardView;
        @BindView(R.id.imgCardInfoRel)
        ImageView imageView;
        @BindView(R.id.tv_info_rel_tit)
        TextView title;
        @BindView(R.id.state)
        ImageView state;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}