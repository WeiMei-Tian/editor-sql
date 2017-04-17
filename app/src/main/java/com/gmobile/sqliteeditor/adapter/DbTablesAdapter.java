package com.gmobile.sqliteeditor.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.widget.CircleLayout;

import java.util.List;


/**
 * Created by admin on 2016/9/20.
 */
public class DbTablesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity mActivity;
    private List<String> mDatas;
    private TabClickListener mTabClickListener;
    private TabLongClickListener mTabLongClickListener;
    private TabMoreClickListener mMoreClickListener;
    private int longClickPosition = -1;
    private int mPayItemSize;

    public final int TYPE_PAY = 0;
    public final int TYPE_NORMAL = 1;

    public DbTablesAdapter(Activity act) {
        this.mActivity = act;
        mPayItemSize = getPayItemSize();
    }

    @Override
    public int getItemViewType(int position) {
        if (mPayItemSize > 0){
            if (position == 0) {
                return TYPE_PAY;
            } else {
                return TYPE_NORMAL;
            }
        }

        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_PAY) {
            return new InnerPayViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.sql_tabs_pay_layout, null));
        } else {
            return new InnerViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.sql_db_tabs_item, null));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (mPayItemSize > 0 && position == 0) {

            InnerPayViewHolder PayViewHolder = (InnerPayViewHolder) holder;
            PayViewHolder.open.setOnClickListener(onPayClick);
            PayViewHolder.itemView.setOnClickListener(onPayClick);

        } else {

            final int dataPosition = position - mPayItemSize;

            final InnerViewHolder innerViewHolder = (InnerViewHolder) holder;

            innerViewHolder.name.setText(mDatas.get(dataPosition));
            innerViewHolder.relativeLayout.setTag(dataPosition);

            if (longClickPosition == dataPosition) {
                selectAnim(innerViewHolder, true);
            } else {
                selectAnim(innerViewHolder, false);
            }

            innerViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mTabClickListener != null) {
                        mTabClickListener.click(dataPosition);
                    }
                }
            });

            innerViewHolder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mMoreClickListener != null){
                        mMoreClickListener.moreClick(innerViewHolder.more,position);
                    }
                }
            });

            innerViewHolder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (mTabLongClickListener != null) {
                        longClickPosition = dataPosition;
                        mTabLongClickListener.longClick(dataPosition);
                    }
                    return true;
                }
            });
        }
    }

    View.OnClickListener onPayClick = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
//            if (!Utils.goToBuySqlite(mActivity)){
//                //TODO 需要刷新页面
//                refreshData();
//            }
        }
    };

    public void clearEditType() {

        longClickPosition = -1;
        refreshData();
    }

    public void refreshData(){
        mPayItemSize = getPayItemSize();
        notifyDataSetChanged();
    }

    public int getPayItemSize(){
//        int result = FeFunBase.isSpeciallyNeedBuy(PurchaseConstant.FUN_SQLEDITOR);
//        if (result != PurchaseConstant.DONT_NEED_BUY) {
//            return 1;
//        }

        return 0;
    }

    /**
     * 长按选中thumb动画
     */
    private void selectAnim(final InnerViewHolder holder, boolean isShowAnim) {

        if (isShowAnim) {

            holder.circleLayout.setVisibility(View.VISIBLE);
            final CircleLayout circleLayout = holder.circleLayout;
            ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(circleLayout, "alpha", 0f);
            ObjectAnimator scaleXOutAnim = ObjectAnimator.ofFloat(circleLayout, "scaleX", 0f);
            ObjectAnimator scaleYOutAnim = ObjectAnimator.ofFloat(circleLayout, "scaleY", 0f);

            ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(circleLayout, "alpha", 1f);
            ObjectAnimator scaleXInAnim = ObjectAnimator.ofFloat(circleLayout, "scaleX", 1.1f);
            ObjectAnimator scaleYInAnim = ObjectAnimator.ofFloat(circleLayout, "scaleY", 1.1f);

            ObjectAnimator scaleXIn1Anim = ObjectAnimator.ofFloat(circleLayout, "scaleX", 1f);
            ObjectAnimator scaleYIn1Anim = ObjectAnimator.ofFloat(circleLayout, "scaleY", 1f);

            alphaOutAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    circleLayout.setColor(mActivity.getResources().getColor(R.color.blue));
                    holder.thumb.setVisibility(View.VISIBLE);
                    holder.thumb.setImageResource(R.drawable.ic_done_navbar);
                }
            });

            AnimatorSet animSet = new AnimatorSet();
            animSet.setDuration(120);
            animSet.play(alphaOutAnim).with(scaleXOutAnim).with(scaleYOutAnim);
            animSet.play(alphaInAnim).with(scaleXInAnim).with(scaleYInAnim).after(alphaOutAnim);
            animSet.play(scaleXIn1Anim).with(scaleYIn1Anim).after(alphaInAnim);
            animSet.start();
        } else {
            holder.circleLayout.setVisibility(View.GONE);
            holder.thumb.setVisibility(View.GONE);
            holder.circleLayout.setColor(mActivity.getResources().getColor(R.color.transparent));
        }

    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() + mPayItemSize : 0;
    }

    public void setDatas(List<String> datas) {
        this.mDatas = datas;
        notifyDataSetChanged();
    }

    public void setTabClickListener(TabClickListener listener) {
        this.mTabClickListener = listener;
    }

    public void setTabLongClickListener(TabLongClickListener listener) {
        this.mTabLongClickListener = listener;
    }

    public void setmMoreClickListener(TabMoreClickListener mMoreClickListener) {
        this.mMoreClickListener = mMoreClickListener;
    }

    public interface TabClickListener {
        void click(int position);
    }

    public interface TabMoreClickListener {
        void moreClick(View view,int position);
    }

    public interface TabLongClickListener {
        void longClick(int position);
    }

    public class InnerViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView name;
        public RelativeLayout relativeLayout;
        public CircleLayout circleLayout;
        public ImageView thumb;
        public ImageView more;

        public InnerViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.itemView);
            circleLayout = (CircleLayout) itemView.findViewById(R.id.thumb_circle_layout);
            thumb = (ImageView) itemView.findViewById(R.id.item_thumb);
            more = (ImageView) itemView.findViewById(R.id.item_more);
        }
    }

    public class InnerPayViewHolder extends RecyclerView.ViewHolder {

        public TextView msg;
        public TextView open;

        public InnerPayViewHolder(View itemView) {
            super(itemView);
            msg = (TextView) itemView.findViewById(R.id.tv_msg);
            open = (TextView) itemView.findViewById(R.id.tv_update);
        }
    }
}
