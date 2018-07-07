package com.software.headinthecloudsstudio.gogglemapswithnotes;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SlideShowFragment extends DialogFragment implements GalleryStripAdapter.GalleryStripCallBacks {
    //declare static variable which will serve as key of current position argument
    private static final String ARG_CURRENT_POSITION = "position";
    //Declare list of GalleryItems
    List<GalleryItem> galleryItems;
    //Deceleration of  Gallery Strip Adapter
    GalleryStripAdapter mGalleryStripAdapter;
    // //Deceleration of  Slide show View Pager Adapter
    SlideShowPagerAdapter mSlideShowPagerAdapter;
    //Deceleration of viewPager
    ViewPager mViewPagerGallery;
    RecyclerView recyclerViewGalleryStrip;

    private int mCurrentPosition;
    //set bottom to visible of first load
    boolean isBottomBarVisible = true;


    public SlideShowFragment() {
        // Required empty public constructor
    }

    //This method will create new instance of SlideShowFragment
    public static SlideShowFragment newInstance(int position) {
        SlideShowFragment fragment = new SlideShowFragment();
        //Create bundle
        Bundle args = new Bundle();
        //put Current Position in the bundle
        args.putInt(ARG_CURRENT_POSITION, position);
        //set arguments of SlideShowFragment
        fragment.setArguments(args);
        //return fragment instance
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialise GalleryItems List
        galleryItems = new ArrayList<>();
        if (getArguments() != null) {
            //get Current selected position from arguments
            mCurrentPosition = getArguments().getInt(ARG_CURRENT_POSITION);
            //get GalleryItems from activity
            galleryItems = ((MapsActivity) getActivity()).galleryItems;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_silde_show, container, false);
        mViewPagerGallery = view.findViewById(R.id.viewPagerGallery);
        //Initialise View Pager Adapter
        mSlideShowPagerAdapter = new SlideShowPagerAdapter(getActivity(), galleryItems);
        //set adapter to Viewpager
        mViewPagerGallery.setAdapter(mSlideShowPagerAdapter);
        recyclerViewGalleryStrip = view.findViewById(R.id.recyclerViewGalleryStrip);
        //Create GalleryStripRecyclerView's Layout manager
        final RecyclerView.LayoutManager mGalleryStripLayoutManger = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        //set layout manager of GalleryStripRecyclerView
        recyclerViewGalleryStrip.setLayoutManager(mGalleryStripLayoutManger);
        //Create GalleryStripRecyclerView's Adapter
        mGalleryStripAdapter = new GalleryStripAdapter(galleryItems, getActivity(), this, mCurrentPosition);
        //set Adapter of GalleryStripRecyclerView
        recyclerViewGalleryStrip.setAdapter(mGalleryStripAdapter);
        //tell viewpager to open currently selected item and pass position of current item
        mViewPagerGallery.setCurrentItem(mCurrentPosition);
        //Add OnPageChangeListener to viewpager to handle page changes
        mViewPagerGallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //first check When Page is scrolled and gets stable
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    //get current  item on view pager
                    int currentSelected = mViewPagerGallery.getCurrentItem();
                    //scroll strip smoothly to current  position of viewpager
                    mGalleryStripLayoutManger.smoothScrollToPosition(recyclerViewGalleryStrip, null, currentSelected);
                    //select current item of viewpager on gallery strip at bottom
                    mGalleryStripAdapter.setSelected(currentSelected);

                }

            }
        });
        return view;
    }

    //Overridden method by GalleryStripAdapter.GalleryStripCallBacks for communication on gallery strip item selected
    @Override
    public void onGalleryStripItemSelected(int position) {
        //set current item of viewpager
        mViewPagerGallery.setCurrentItem(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //remove selection on destroy
        mGalleryStripAdapter.removeSelection();
    }
}
