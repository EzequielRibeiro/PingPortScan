package org.ping.cool;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import org.ping.cool.databinding.FragmentSecondBinding;
;import static org.ping.cool.MainActivity.isOnline;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

       // startCheckPort(new String[]{"-h www.facebook.com","-t 150","-th 100","-p 438-446"});
       if(isOnline(getActivity().getApplicationContext())){
           new CheckPort(new String[]{"-h www.facebook.com","-t 150","-th 100","-p 438-446"});
       }else{
           Toast.makeText(getActivity(), "without internet connection", Toast.LENGTH_SHORT).show();
       }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}