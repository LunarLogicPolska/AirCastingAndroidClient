package pl.llp.aircasting.screens.dashboard.views;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;

import static pl.llp.aircasting.screens.dashboard.adapters.ViewingStreamsRecyclerAdapter.STREAM_CHART_HEIGHT;
import static pl.llp.aircasting.screens.dashboard.adapters.ViewingStreamsRecyclerAdapter.STREAM_CHART_WIDTH;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.REORDER_IN_PROGRESS;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SENSOR;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_ID;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.STREAM_CHART;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.STREAM_IDENTIFIER;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.STREAM_TIMESTAMP;

public class ViewingStreamItemViewMvcImpl implements StreamItemViewMvc {
    private final View mRootView;
    private final RelativeLayout mSessionTitleContainer;
    private final TextView mSensorNameTv;
    private final TextView mNowTv;
    private final TextView mTimestampTv;
    private final TextView mSessionTitleTv;
    private final LinearLayout mSessionButtonsLayout;
    private final RelativeLayout mChartLayout;

    private List<Listener> mListeners = new ArrayList<>();

    private Sensor mSensor;
    private Session mSession;
    private String mNowValue = "0";
    private ResourceHelper mResourceHelper;
    private Boolean mSessionReorderInProgress = false;
    private String mSensorNameText;
    private Boolean mSessionRecording;
    private long mSessionId;
    private int mPosition;
    private LineChart mChart;
    private String mStreamIdentifier;
    private Date mStreamTimestamp;

    public ViewingStreamItemViewMvcImpl(LayoutInflater inflater, ViewGroup parent) {
        mRootView = inflater.inflate(R.layout.stream_row, parent, false);
        mSessionTitleContainer = findViewById(R.id.title_container);

        mSensorNameTv = findViewById(R.id.sensor_name);
        mNowTv = findViewById(R.id.now);
        mTimestampTv = findViewById(R.id.timestamp);
        mSessionTitleTv = mSessionTitleContainer.findViewById(R.id.session_title);
        mSessionButtonsLayout = mSessionTitleContainer.findViewById(R.id.session_reorder_buttons);

        mChartLayout = findViewById(R.id.chart_layout);

        getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Listener listener : mListeners) {
                    listener.onStreamClicked(v);
                }
            }
        });
    }

    @Override
    public void bindData(Map<String, Object> dataItem, int position, ResourceHelper resourceHelper) {
        mPosition = position;
        mSensor = (Sensor) dataItem.get(SENSOR);
        mSensorNameText = mSensor.getSensorName();
        mSession = (Session) dataItem.get(SESSION);
        mSessionId = (long) dataItem.get(SESSION_ID);
        mSessionReorderInProgress = (Boolean) dataItem.get(REORDER_IN_PROGRESS);
        mChart = (LineChart) dataItem.get(STREAM_CHART);
        mStreamIdentifier = (String) dataItem.get(STREAM_IDENTIFIER);
        mStreamTimestamp = (Date) dataItem.get(STREAM_TIMESTAMP);
        mResourceHelper = resourceHelper;

        Log.w("bindData", "------------------");
        Log.w(String.valueOf(mSessionId), mSensorNameText);
        Log.w("chart", String.valueOf(mChart));
        Log.w("chart", String.valueOf(mChart.getData()));
        Log.w("bindData", "------------------");

        drawFullView();
    }

    @Override
    public void bindSessionTitle(int position) {
        mPosition = position;
        setTitleView();
    }

    @Override
    public void bindNowValue(Map<String, Double> nowValues) {

    }

    @Override
    public void bindChart(Map mChartData) {
        Log.w("stream view", "bind chart");
        mChart = (LineChart) mChartData.get(mStreamIdentifier);

        if (mChart != null) {
            if (mChart.getParent() != null) {
                ((ViewGroup) mChart.getParent()).removeView(mChart);
            }

            mChartLayout.addView(mChart, mChartLayout.getWidth(), mChartLayout.getHeight());
        }
    }

    private void drawFullView() {
        Log.w("stream view", "full draw");
        if (mChart.getParent() != null) {
            ((ViewGroup) mChart.getParent()).removeView(mChart);
        }

        mRootView.setTag(R.id.session_id_tag, mSessionId);
        showAndSetTimestamp();
        setTitleView();
        mSensorNameTv.setText(mSensorNameText);
        setBackground();
        mNowTv.setText(mNowValue);

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STREAM_CHART_WIDTH, getRootView().getContext().getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STREAM_CHART_HEIGHT, getRootView().getContext().getResources().getDisplayMetrics());

        ViewGroup.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        mChart.setLayoutParams(params);
        mChartLayout.addView(mChart);
    }

    private void setTitleView() {
        if (positionWithTitle()) {

            mSessionTitleContainer.setVisibility(View.VISIBLE);
            mSessionTitleTv.setCompoundDrawablesWithIntrinsicBounds(mSession.getDrawable(), 0, 0, 0);

            if (mSessionReorderInProgress) {
                mSessionButtonsLayout.setVisibility(View.VISIBLE);
            } else {
                mSessionButtonsLayout.setVisibility(View.GONE);
            }

            if (!mSession.hasTitle()) {
                mSessionTitleTv.setText(R.string.unnamed);
            } else {
                mSessionTitleTv.setText(mSession.getTitle());
            }
        } else {
            mSessionTitleContainer.setVisibility(View.GONE);
        }
    }

    private boolean positionWithTitle() {
        return mPosition == 0;
    }

    private void setBackground() {
        mNowTv.setBackground(mResourceHelper.getStreamValueBackground(mSensor, Double.parseDouble(mNowValue)));
    }

    private void showAndSetTimestamp() {
        mTimestampTv.setVisibility(View.VISIBLE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm MM/dd/yy");
        mTimestampTv.setText(dateFormat.format(mStreamTimestamp));
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public void registerListener(Listener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterListener(Listener listener) {
        mListeners.remove(listener);
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }
}
