package co.com.sersoluciones.pruebaapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import co.com.sersoluciones.pruebaapplication.R;

/**
 * Created by Gustavo on 9/07/2018.
 */

public class MainFragment extends Fragment {


    private EditText editText1;
    private EditText editText2;
    private OnMainFragmentListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.main_fragment, container, false);

        editText1 = view.findViewById(R.id.editTextnumber1);
        editText2 = view.findViewById(R.id.editTextnumber2);
        final TextView textView = view.findViewById(R.id.textView);
        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String numStr1 = editText1.getText().toString();
                String numStr2 = editText2.getText().toString();
                if (validityFields()) {

                    int num1 = Integer.valueOf(numStr1);
                    int num2 = Integer.parseInt(numStr2);

                    int res = num1 + num2;
                    textView.setText(String.format("Resultado: %s", res));

                    mListener.onResult(String.format("Resultado: %s", res));
                }
            }
        });

        return view;
    }

    private boolean validityFields() {
        String numStr1 = editText1.getText().toString();
        String numStr2 = editText2.getText().toString();
        boolean flag = true;
        if (numStr1.isEmpty()) {
            editText1.setError("Este campo es obligatorio");
            editText1.requestFocus();
            flag = false;
        } else if (numStr2.equals("")) {
            editText2.setError("Este campo es obligatorio");
            editText2.requestFocus();
            flag = false;
        }

        return flag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMainFragmentListener) {
            mListener = (OnMainFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMainFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMainFragmentListener{

        void onResult(String value);
    }
}
