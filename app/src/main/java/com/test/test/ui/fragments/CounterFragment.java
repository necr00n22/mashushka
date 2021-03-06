package com.test.test.ui.fragments;


import android.app.AlertDialog;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.test.test.R;
import com.test.test.data.entity.CommentEntity;
import com.test.test.data.entity.CounterEntity;
import com.test.test.ui.CommentsProvider;
import com.test.test.ui.activities.CommentActivity;
import com.test.test.ui.adapters.CounterAdapter;
import com.test.test.viewmodel.CounterViewModel;
import com.test.test.viewmodel.factory.CounterViewModelFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CounterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CounterFragment extends Fragment implements CommentsProvider{

    private static final String ID = "id";

    @BindView(R.id.list)
    RecyclerView list;

    private CounterViewModel mCountersListViewModel;
    private long id = -1;
    private CounterAdapter adapter;
    private CounterEntity counter;

    public CounterFragment() {
        // Required empty public constructor
    }


    public static CounterFragment newInstance(long id) {
        CounterFragment fragment = new CounterFragment();
        Bundle args = new Bundle();
        args.putLong(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.counter_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete:
                showDeleteCounterDialog();
                return true;
        }

        return false;
    }

    private void showDeleteCounterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setMessage(R.string.dialog_delete_description);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteCounter();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void deleteCounter() {
        mCountersListViewModel.deleteCounter();
        getActivity().onBackPressed();
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        adapter = new CounterAdapter(getContext(), new ArrayList<>(), this);
        list.setLayoutManager(llm);
        list.setAdapter(adapter);


        mCountersListViewModel.getCounter().observe(this, new Observer<CounterEntity>() {
            @Override
            public void onChanged(@Nullable CounterEntity counterEntity) {
                counter = counterEntity;
                if(counter.getBlocks() != null && !counter.getBlocks().isEmpty())
                    adapter.setItems(counterEntity.getBlocks());
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle bundle = getArguments();
        id = bundle.getLong(ID, -1L);
        CounterViewModelFactory factory = new CounterViewModelFactory(getActivity().getApplication(), id);
        mCountersListViewModel = ViewModelProviders.of(this,factory).get(CounterViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public LiveData<List<CommentEntity>> getComments() {
        return mCountersListViewModel.getComments();
    }

    @Override
    public void addComment(CommentEntity comment) {
        comment.setCounterCreatedDate(new Date(counter.getCreateDate()));
        comment.setCounterId(counter.getId());
        comment.setCreatedDate(new Date());

        mCountersListViewModel.addComment(comment);
    }

    @Override
    public void showComment(long commentId) {

    }

    @Override
    public void showAllComments() {
        startActivity(CommentActivity.createShowAllCommentsIntent(getContext(), counter.getId()));
    }

    @Override
    public LifecycleOwner getLyfecycleOwner() {
        return this;
    }
}
