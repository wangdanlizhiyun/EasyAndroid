package com.ayvytr.easyandroidtest.stickyheader.itemdecoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ayvytr.logger.L;

/**
 * Desc:
 * Date: 2017/4/17
 *
 * @author davidwang
 */

public class HeaderItemDecoration extends RecyclerView.ItemDecoration
{
    private StickyItemHeaderAdapter headerAdapter;
    private HeaderViewCache headerViewCache;
    private boolean isStick = true;

    private static final int NO_POSITION = RecyclerView.NO_POSITION;
    private Rect rect;

    public HeaderItemDecoration(StickyItemHeaderAdapter headerAdapter)
    {
        this.headerAdapter = headerAdapter;
        headerViewCache = new HeaderViewCache(headerAdapter);
        ((RecyclerView.Adapter) headerAdapter)
                .registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
                {
                    @Override
                    public void onChanged()
                    {
                        headerViewCache.invalidate();
                    }
                });
        rect = new Rect();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state)
    {
        super.onDrawOver(c, parent, state);
        draw(c, parent);
    }

    private void draw(Canvas c, RecyclerView parent)
    {
        int childCount = parent.getChildCount();

        int top = parent.getPaddingTop();
        int groupId = NO_POSITION;
        int preGroupId;
        int x = parent.getPaddingLeft();
        int y;
        for(int i = 0; i < childCount; i++)
        {
            View itemView = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(itemView);
            parent.getDecoratedBoundsWithMargins(itemView, rect);

            //只有各组第一个 并且 groupId!=-1 才绘制头部view
            preGroupId = groupId;
            groupId = getHeaderId(position);
            if(groupId <= NO_POSITION || groupId == preGroupId)
            {
                continue;
            }

            View header = getHeaderView(parent, position);

            y = rect.top;
            if(isStick)
            {
                y = Math.max(header.getBottom(), itemView.getTop());
                L.e("isStick", y);
                int nextPosition = getNextHeadPosition(i, groupId, childCount, parent);
                if(nextPosition != NO_POSITION)
                {
                    View nextView = parent.getChildAt(nextPosition);
                    //判断下一个头部view是否到了与上一个头部view接触的临界值
                    //如果满足条件则把上一个头部view推上去
                    if(nextView.getTop() <= header.getBottom())
                    {
                        L.e("推", header.getBottom(), y);
                        y = nextView.getTop() - header.getBottom();
                    }
                }
            }

            c.translate(x, y);
            header.draw(c);
            c.translate(-x, -y);
        }
    }

    /**
     * 获取下一个节点，如果没有则返回-1
     *
     * @param count
     * @return
     */
    private int getNextHeadPosition(int id, int groupId, int count, RecyclerView parent)
    {
        for(int i = id; i < count; i++)
        {
            if(headerAdapter.getId(parent.getChildAdapterPosition(parent.getChildAt(i))) != groupId)
            {
                return i;
            }
        }
        return -1;
    }

    private boolean canDraw(int position)
    {
        int headerId = getHeaderId(position);
        int preHeaderId = getPreHeaderId(position);

        return headerId >= 0 && headerId != preHeaderId;
    }

    private int getPreHeaderId(int position)
    {
        if(position < 1)
        {
            return -1;
        }

        return headerAdapter.getId(position - 1);
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);
        setItemOffsets(outRect, view, parent);
    }

    private void setItemOffsets(Rect outRect, View view, RecyclerView parent)
    {
        int position = parent.getChildAdapterPosition(view);
        if(hasItemOffset(position))
        {
            int height = getHeaderView(parent, position).getHeight();
            outRect.set(0, height, 0, 0);
        }
    }

    private boolean hasItemOffset(int position)
    {
        int id = headerAdapter.getId(position);
        if(position == 0)
        {
            return id >= 0;
        }

        int preId = headerAdapter.getId(position - 1);
        return id >= 0 && id != preId;
    }

    private int getHeaderId(int position)
    {
        try
        {
            return headerAdapter.getId(position);
        } catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean hasHeader(int position)
    {
        int id = getHeaderId(position);
        if(position == 0)
        {
            return id > NO_POSITION;
        }

        int preId = getHeaderId(position - 1);
        return id > NO_POSITION && id != preId;
    }

    /**
     * 获取HeaderView
     *
     * @param parent
     * @param position RecyclerView当前Item的position，需要用 {@link RecyclerView#getChildAdapterPosition(View)}
     *                 获得真实position，不然显示的数据有误
     * @return Header view
     */
    public View getHeaderView(RecyclerView parent, int position)
    {
        return headerViewCache.getHeader(parent, position);
    }
}