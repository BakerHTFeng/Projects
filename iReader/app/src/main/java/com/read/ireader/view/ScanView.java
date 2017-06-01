package com.read.ireader.view;

/**
 * Created by Andy on 2017/5/15.
 */

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.read.ireader.R;
import com.read.ireader.tools.TransTask;

public class ScanView extends RelativeLayout
{
    public static final String TAG = "ScanView";
    private boolean isInit = true;
    // 滑动的时候存在两页可滑动，要判断是哪一页在滑动
    private boolean isPreMoving = true, isCurrMoving = true;
    // 当前是第几页
    private int index;
    private float lastX;
    // 前一页，当前页，下一页的左边位置
    private int prePageLeft = 0, currPageLeft = 0, nextPageLeft = 0;
    // 三张页面
    private View prePage, currPage, nextPage;
    // 页面状态
    private static final int STATE_MOVE = 0;
    private static final int STATE_STOP = 1;
    // 滑动的页面，只有前一页和当前页可滑
    private static final int PRE = 2;
    private static final int CURR = 3;
    private int state = STATE_STOP;
    // 正在滑动的页面右边位置，用于绘制阴影
    private float right;
    // 手指滑动的距离
    private float moveLenght;
    // 页面宽高
    private int mWidth, mHeight;
    // 获取滑动速度
    private VelocityTracker vt;
    // 防止抖动
    private float speed_shake = 20;
    // 当前滑动速度
    private float speed;
    private Timer timer;
    private static final int REST_TIME = 10000;
    private long init_time = new Date().getTime();
    private MyTimerTask mTask;
    // 滑动动画的移动速度
    public static final int MOVE_SPEED = 10;
    // 页面适配器
    private PageAdapter adapter;
    /**
     * 过滤多点触碰的控制变量
     */
    private int mEvents;

    public void setAdapter(ScanViewAdapter adapter,final int index) {
        this.index = index;
        removeAllViews();
        this.adapter = adapter;
        prePage = adapter.getView();
        setCustomSelectionActionModeCallback(prePage);
        addView(prePage, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        adapter.addContent(prePage, index - 1);

        currPage = adapter.getView();
        setCustomSelectionActionModeCallback(currPage);
        addView(currPage, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        adapter.addContent(currPage, index);

        nextPage = adapter.getView();
        setCustomSelectionActionModeCallback(nextPage);
        addView(nextPage, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        adapter.addContent(nextPage, index + 1);

    }

    /**
     * 向左滑。注意可以滑动的页面只有当前页和前一页
     *
     * @param which
     */
    private void moveLeft(int which) {
        long time = new Date().getTime();
        if(time - init_time > REST_TIME) {
            alertReadingTime();
            init_time = time;
        }
        switch (which) {
            case PRE:
                prePageLeft -= MOVE_SPEED;
                if (prePageLeft < -mWidth)
                    prePageLeft = -mWidth;
                right = mWidth + prePageLeft;
                break;
            case CURR:
                currPageLeft -= MOVE_SPEED;
                if (currPageLeft < -mWidth)
                    currPageLeft = -mWidth;
                right = mWidth + currPageLeft;
                break;
        }
    }

    /**
     * 向右滑。注意可以滑动的页面只有当前页和前一页
     *
     * @param which
     */
    private void moveRight(int which) {
        long time = new Date().getTime();
        if(time - init_time > REST_TIME) {
            alertReadingTime();
            init_time = time;
        }
        switch (which) {
            case PRE:
                prePageLeft += MOVE_SPEED;
                if (prePageLeft > 0)
                    prePageLeft = 0;
                right = mWidth + prePageLeft;
                break;
            case CURR:
                currPageLeft += MOVE_SPEED;
                if (currPageLeft > 0)
                    currPageLeft = 0;
                right = mWidth + currPageLeft;
                break;
        }
    }

    /**
     * 当往回翻过一页时添加前一页在最左边
     */
    private void addPrePage() {
        removeView(nextPage);
        addView(nextPage, -1, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        // 从适配器获取前一页内容
        adapter.addContent(nextPage, index - 1);
        // 交换顺序
        View temp = nextPage;
        nextPage = currPage;
        currPage = prePage;
        prePage = temp;
        prePageLeft = -mWidth;
    }

    /**
     * 当往前翻过一页时，添加一页在最底下
     */
    private void addNextPage() {
        removeView(prePage);
        addView(prePage, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        // 从适配器获取后一页内容
        adapter.addContent(prePage, index + 1);
        // 交换顺序
        View temp = currPage;
        currPage = nextPage;
        nextPage = prePage;
        prePage = temp;
        currPageLeft = 0;
    }

    Handler updateHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (state != STATE_MOVE)
                return;
            // 移动页面
            // 翻回，先判断当前哪一页处于未返回状态
            if (prePageLeft > -mWidth && speed <= 0) {
                // 前一页处于未返回状态
                moveLeft(PRE);
            } else if (currPageLeft < 0 && speed >= 0) {
                // 当前页处于未返回状态
                moveRight(CURR);
            } else if (speed < 0 && index < adapter.getCount()) {
                // 向左翻，翻动的是当前页
                moveLeft(CURR);
                if (currPageLeft == (-mWidth))
                {
                    index++;
                    // 翻过一页，在底下添加一页，把最上层页面移除
                    addNextPage();
                }
            } else if (speed > 0 && index > 1) {
                // 向右翻，翻动的是前一页
                moveRight(PRE);
                if (prePageLeft == 0)
                {
                    index--;
                    // 翻回一页，添加一页在最上层，隐藏在最左边
                    addPrePage();
                }
            }
            if (right == 0 || right == mWidth) {
                releaseMoving();
                state = STATE_STOP;
                quitMove();
            }
            ScanView.this.requestLayout();
        }

    };

    public ScanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ScanView(Context context) {
        super(context);
        init();
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 退出动画翻页
     */
    public void quitMove() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    private void init() {
        index = 1;
        timer = new Timer();
        mTask = new MyTimerTask(updateHandler);
    }

    /**
     * 释放动作，不限制手滑动方向
     */
    private void releaseMoving() {
        isPreMoving = true;
        isCurrMoving = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (adapter != null)
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX();
                    try {
                        if (vt == null) {
                            vt = VelocityTracker.obtain();
                        } else {
                            vt.clear();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    vt.addMovement(event);
                    mEvents = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_UP:
                    mEvents = -1;
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 取消动画
                    quitMove();
                    Log.d("index", "mEvents = " + mEvents + ", isPreMoving = "
                            + isPreMoving + ", isCurrMoving = " + isCurrMoving);
                    vt.addMovement(event);
                    vt.computeCurrentVelocity(500);
                    speed = vt.getXVelocity();
                    moveLenght = event.getX() - lastX;
                    if ((moveLenght > 0 || !isCurrMoving) && isPreMoving
                            && mEvents == 0) {
                        isPreMoving = true;
                        isCurrMoving = false;
                        if (index == 1) {
                            // 第一页不能再往右翻，跳转到前一个activity
                            state = STATE_MOVE;
                            showInfoMsg("当前是第一页！");
                            releaseMoving();
                        } else {
                            // 非第一页
                            prePageLeft += (int) moveLenght;
                            // 防止滑过边界
                            if (prePageLeft > 0)
                                prePageLeft = 0;
                            else if (prePageLeft < -mWidth) {
                                // 边界判断，释放动作，防止来回滑动导致滑动前一页时当前页无法滑动
                                prePageLeft = -mWidth;
                                releaseMoving();
                            }
                            right = mWidth + prePageLeft;
                            state = STATE_MOVE;
                        }
                    } else if ((moveLenght < 0 || !isPreMoving) && isCurrMoving
                            && mEvents == 0) {
                        isPreMoving = false;
                        isCurrMoving = true;
                        if (index == adapter.getCount()) {
                            // 最后一页不能再往左翻
                            state = STATE_STOP;
                            showInfoMsg("当前是最后一页！");
                            releaseMoving();
                        } else {
                            currPageLeft += (int) moveLenght;
                            // 防止滑过边界
                            if (currPageLeft < -mWidth)
                                currPageLeft = -mWidth;
                            else if (currPageLeft > 0) {
                                // 边界判断，释放动作，防止来回滑动导致滑动当前页是前一页无法滑动
                                currPageLeft = 0;
                                releaseMoving();
                            }
                            right = mWidth + currPageLeft;
                            state = STATE_MOVE;
                        }

                    } else
                        mEvents = 0;
                    lastX = event.getX();
                    requestLayout();
                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(speed) < speed_shake)
                        speed = 0;
                    quitMove();
                    mTask = new MyTimerTask(updateHandler);
                    timer.schedule(mTask, 0, 5);
                    try
                    {
                        vt.clear();
                        vt.recycle();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        super.dispatchTouchEvent(event);
        return true;
    }

    /*
     * （非 Javadoc） 在这里绘制翻页阴影效果
     *
     * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (right == 0 || right == mWidth)
            return;
        RectF rectF = new RectF(right, 0, mWidth, mHeight);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        LinearGradient linearGradient = new LinearGradient(right, 0,
                right + 36, 0, 0xffbbbbbb, 0x00bbbbbb, TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setStyle(Style.FILL);
        canvas.drawRect(rectF, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        if (isInit) {
            // 初始状态，一页放在左边隐藏起来，两页叠在一块
            prePageLeft = -mWidth;
            currPageLeft = 1;
            nextPageLeft = 1;
            isInit = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (adapter == null)
            return;
        prePage.layout(prePageLeft, 0,
                prePageLeft + prePage.getMeasuredWidth(),
                prePage.getMeasuredHeight());
        currPage.layout(currPageLeft, 0,
                currPageLeft + currPage.getMeasuredWidth(),
                currPage.getMeasuredHeight());
        nextPage.layout(nextPageLeft, 0,
                nextPageLeft + nextPage.getMeasuredWidth(),
                nextPage.getMeasuredHeight());
        invalidate();
    }

    private void showInfoMsg(String msg) {
        Toast toast = new Toast(this.getContext());
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.toast_view, null);
        TextView message = (TextView) view.findViewById(R.id.toast);
        message.setText(msg);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    public void setCustomSelectionActionModeCallback(View view){
        final TextView content_view = (TextView)view.findViewById(R.id.content);
        ActionMode.Callback2 textSelectionActionModeCallback;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            textSelectionActionModeCallback = new ActionMode.Callback2() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    menu.clear();
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menuInflater.inflate(R.menu.context_menu,menu);
                    return true; //返回false则不会显示弹窗
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    //根据item的ID处理点击事件
                    switch (menuItem.getItemId()){
                        case R.id.note:
                            actionMode.finish();//收起操作菜单
                            break;
                        case R.id.trans:
                            String selected = content_view.getText()
                                    .subSequence(content_view.getSelectionStart(),
                                            content_view.getSelectionEnd())
                                    .toString();
                            showTrans(selected);
                            actionMode.finish();
                            break;
                    }
                    return false;//返回true则系统的"复制"、"搜索"之类的item将无效，只有自定义item有响应
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {

                }

                @Override
                public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
                    //可选  用于改变弹出菜单的位置
                    super.onGetContentRect(mode, view, outRect);
                }
            };
            content_view.setCustomSelectionActionModeCallback(textSelectionActionModeCallback);
        }
    }
    private void alertReadingTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage("阅读时间过长，请休息您的眼睛！");
        builder.setTitle("提示");
        builder.setNegativeButton("我就是要继续阅读", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private void showTrans(String selected) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        View trans_view = LayoutInflater.from(this.getContext()).inflate(R.layout.trans_view, null);
        builder.setView(trans_view);
        builder.setNegativeButton("返回阅读", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        TransTask task = new TransTask(this.getContext(), trans_view);
        task.execute(selected);
        builder.create();
        builder.show();
    }

    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler)
        {
            this.handler = handler;
        }

        @Override
        public void run()
        {
            handler.sendMessage(handler.obtainMessage());
        }

    }
}