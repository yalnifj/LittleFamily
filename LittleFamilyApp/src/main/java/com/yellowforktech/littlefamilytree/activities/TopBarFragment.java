package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopBarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopBarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopBarFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PERSON = "person";
    public static final String ARG_LAYOUT = "layout";

    private LittlePerson person;

    private OnFragmentInteractionListener mListener;

    private ImageView homeButton;
    private ImageView profileButton;
    private Context context;
    private int layoutResource;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment TopBarFragment.
     */
    public static TopBarFragment newInstance(LittlePerson param1) {
        TopBarFragment fragment = new TopBarFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PERSON, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public static TopBarFragment newInstance(LittlePerson param1, int layoutResource) {
        TopBarFragment fragment = new TopBarFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PERSON, param1);
        args.putSerializable(ARG_LAYOUT, layoutResource);
        fragment.setArguments(args);
        return fragment;
    }

    public TopBarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutResource = com.yellowforktech.littlefamilytree.R.layout.fragment_top_bar;
        if (getArguments() != null) {
            person = (LittlePerson) getArguments().getSerializable(ARG_PERSON);
            Integer resId = (Integer) getArguments().getSerializable(ARG_LAYOUT);
            if (resId != null) {
                layoutResource = resId;
            }
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(layoutResource, container, false);
        homeButton = (ImageView) view.findViewById(com.yellowforktech.littlefamilytree.R.id.homeButton);
        profileButton = (ImageView) view.findViewById(com.yellowforktech.littlefamilytree.R.id.profileButton);

        updatePerson(person);
        return view;
    }

    public void updatePerson(LittlePerson person) {
        this.person = person;
        if (person!=null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x / 8;
            if (profileButton!=null) {
                if (person.getPhotoPath() != null) {
                    Bitmap bm = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width, width, false);
                    profileButton.setImageBitmap(bm);
                } else {
                    Bitmap bm = ImageHelper.loadBitmapFromResource(context, person.getDefaultPhotoResource(), 0, width, width);
                    profileButton.setImageBitmap(bm);
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            context = activity;
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

    }

}
