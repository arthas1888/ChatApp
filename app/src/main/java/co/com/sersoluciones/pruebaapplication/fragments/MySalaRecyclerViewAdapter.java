package co.com.sersoluciones.pruebaapplication.fragments;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import co.com.sersoluciones.pruebaapplication.R;
import co.com.sersoluciones.pruebaapplication.fragments.SalaFragment.OnListFragmentInteractionListener;
import co.com.sersoluciones.pruebaapplication.models.Sala;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Sala} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MySalaRecyclerViewAdapter extends RecyclerView.Adapter<MySalaRecyclerViewAdapter.ViewHolder> {

    private final List<Sala> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MySalaRecyclerViewAdapter(List<Sala> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_sala, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNombreView.setText(String.format("%s", mValues.get(position).nombre));
        holder.mDireccionView.setText(mValues.get(position).direccion);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        holder.mMapImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onOpenMap(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mValues == null) return 0;
        return mValues.size();
    }

    public void updateList(ArrayList<Sala> salaList) {
        mValues.clear();
        mValues.addAll(salaList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNombreView;
        public final TextView mDireccionView;
        private final ImageView mMapImageView;
        public Sala mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNombreView = (TextView) view.findViewById(R.id.nombre);
            mDireccionView = (TextView) view.findViewById(R.id.direccion);
            mMapImageView = (ImageView) view.findViewById(R.id.compartir);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDireccionView.getText() + "'";
        }
    }
}
