package co.com.sersoluciones.pruebaapplication.adapters;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.com.sersoluciones.pruebaapplication.R;
import co.com.sersoluciones.pruebaapplication.models.Estudiante;

public class EstudianteAdapterRecycler extends RecyclerView.Adapter<EstudianteAdapterRecycler.ViewHolder> {

    public ArrayList<Estudiante> mItems;
    private OnItemClickEstudiante mListener;

    public EstudianteAdapterRecycler(ArrayList<Estudiante> estudiantes, OnItemClickEstudiante listener){
        mItems = estudiantes;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_estudiante, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mItems.get(position);
        holder.mNameTextView.setText(String.format("%s %s", holder.mItem.getNombre(), holder.mItem.getApellido()));
        holder.mCourseTextView.setText(holder.mItem.getCurso());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onClickEstudiante(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void updateList(){
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public final View mView;
        private final TextView mNameTextView;
        private final TextView mCourseTextView;
        private Estudiante mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mNameTextView = mView.findViewById(R.id.name);
            mCourseTextView = mView.findViewById(R.id.course);
        }
    }

    public interface OnItemClickEstudiante{
        void onClickEstudiante(Estudiante estudiante);
    }
}
