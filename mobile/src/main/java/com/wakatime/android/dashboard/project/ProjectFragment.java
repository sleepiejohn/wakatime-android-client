package com.wakatime.android.dashboard.project;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.ybq.android.spinkit.SpinKitView;
import com.wakatime.android.R;
import com.wakatime.android.WakatimeApplication;
import com.wakatime.android.dashboard.model.Project;
import com.wakatime.android.support.JsonParser;
import com.wakatime.android.support.view.Animations;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v4.content.ContextCompat.getColor;

/**
 * Project data fragment
 *
 * @author Joao Pedro Evangelista
 */
public class ProjectFragment extends Fragment implements ViewModel {

    public static final String KEY = "project-fragment";

    private static final String ROTATION_CACHE = "rotation-cache";

    @BindView(R.id.recycler_projects)
    RecyclerView mRecyclerProjects;

    @BindView(R.id.loader_projects)
    SpinKitView mLoaderProjects;

    @BindView(R.id.chart_projects)
    PieChart mChartProjects;

    @BindView(R.id.nested_projects)
    NestedScrollView mNestedProjects;

    @Inject
    ProjectPresenter mPresenter;

    private List<Project> rotationCache;

    public ProjectFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment ProjectFragment.
     */
    public static ProjectFragment newInstance() {
        return new ProjectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WakatimeApplication) this.getActivity().getApplication())
                .useDashboardComponent().inject(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnProjectFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnProjectFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(ROTATION_CACHE)) {
            this.rotationCache = JsonParser.read(savedInstanceState.getString(ROTATION_CACHE),
                    new TypeReference<List<Project>>() {
                    });
            this.setProjects(this.rotationCache);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_project, container, false);
        ButterKnife.bind(this, view);
        mPresenter.bind(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey(ROTATION_CACHE)) {
            mPresenter.onInit();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ROTATION_CACHE, JsonParser.write(this.rotationCache));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.onFinish();
        mPresenter.unbind();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return Animations.createMoveAnimation(enter);
    }

    @Override
    public void setProjects(List<Project> projects) {
        setChartData(projects);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ProjectAdapter adapter = new ProjectAdapter(getActivity(), projects);
        mRecyclerProjects.setLayoutManager(layoutManager);
        mRecyclerProjects.setAdapter(adapter);
    }

    @Override
    public void setRotationCache(List<Project> projects) {
        this.rotationCache = projects;
    }

    @Override
    public void hideLoader() {
        this.mLoaderProjects.setVisibility(View.GONE);
        this.mNestedProjects.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoader() {
        this.mLoaderProjects.setVisibility(View.VISIBLE);
        this.mNestedProjects.setVisibility(View.GONE);
    }

    private void setChartData(List<Project> chardData) {

        this.mChartProjects.setDrawHoleEnabled(true);
        this.mChartProjects.setHoleColor(Color.WHITE);
        this.mChartProjects.setTransparentCircleColor(Color.WHITE);
        this.mChartProjects.setTransparentCircleAlpha(110);
        this.mChartProjects.setDragDecelerationFrictionCoef(0.95f);
        this.mChartProjects.setHoleRadius(58f);
        this.mChartProjects.setTransparentCircleRadius(61f);
        this.mChartProjects.setDescription("");
        this.mChartProjects.setUsePercentValues(true);
        this.mChartProjects.setEntryLabelColor(Color.WHITE);
        this.mChartProjects.setDrawCenterText(true);
        this.mChartProjects.setCenterText(getString(R.string.title_projects));
        this.mChartProjects.setCenterTextSize(18f);
        this.mChartProjects.setCenterTextColor(getColor(getActivity(), R.color.colorSecondaryText));
        this.mChartProjects.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        enableNestingScrollForAP21();

        List<PieEntry> entries = new ArrayList<>(chardData.size());
        for (Project project : chardData) {
            entries.add(new PieEntry(project.getPercent(), project.getName()));
        }
        PieDataSet pieDataSet = new PieDataSet(entries, getString(R.string.title_projects));
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(14f);
        pieDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());
        this.mChartProjects.setData(pieData);
    }

    private void enableNestingScrollForAP21() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mChartProjects.setNestedScrollingEnabled(true);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnProjectFragmentInteractionListener {

    }
}