package co.com.sersoluciones.pruebaapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import co.com.sersoluciones.pruebaapplication.fragments.SalaFragment;
import co.com.sersoluciones.pruebaapplication.models.Sala;

public class RequestActivity extends AppCompatActivity implements SalaFragment.OnListFragmentInteractionListener {

    private static final String TAG = "RequestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, new SalaFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void onListFragmentInteraction(Sala item) {

    }

    @Override
    public void onOpenMap(Sala item) {
        Uri gmmIntentUri = Uri.parse(String.format("geo:%s,%s?q=%s,%s(%s)", item.lat, item.lon, item.lat, item.lon, item.nombre));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        //mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);

//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("geo:%s,%s?q=%s,%s(Label+Name)", item.lat,
//                item.lon, item.lat, item.lon)));
//        startActivity(intent);
    }
}
