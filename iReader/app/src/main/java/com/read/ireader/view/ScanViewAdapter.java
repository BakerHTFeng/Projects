package com.read.ireader.view;

/**
 * Created by Andy on 2017/5/15.
 */

import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.read.ireader.R;

public class ScanViewAdapter extends PageAdapter
{
    Context context;
    List<String> items;
    AssetManager am;
    Drawable background;

    public ScanViewAdapter(Context context, List<String> items, Drawable background)
    {
        this.context = context;
        this.items = items;
        this.background = background;
        am = context.getAssets();
    }

    public void addContent(View view, int position)
    {
        TextView content = (TextView) view.findViewById(R.id.content);
        TextView tv = (TextView) view.findViewById(R.id.index);
        TextView tv_max = (TextView) view.findViewById(R.id.max_index);
        if ((position - 1) < 0 || (position - 1) >= getCount())
          return;
        content.setText("    双峰叠障，过天风海雨，无边空碧。月姊年年应好在，玉阙琼宫愁寂。谁唤痴云，一杯未尽，夜气寒无色。碧城凝望，高楼缥缈西北。\n\n    肠断桂冷蟾孤，佳期如梦，又把阑干拍。雾鬓风虔相借问，浮世几回今夕。圆缺睛明，古今同恨，我更长为客。蝉娟明夜，尊前谁念南陌。");
        tv.setText(items.get(position - 1));
        tv_max.setText(" / 第 "+ items.size() + " 页");
    }

    public int getCount()
    {
        return items.size();
    }

    public View getView() {
        View view = LayoutInflater.from(context).inflate(R.layout.page_layout,
                null);
        view.setBackground(background);
        return view;
    }
}