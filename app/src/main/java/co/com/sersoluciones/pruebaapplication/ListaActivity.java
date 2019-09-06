package co.com.sersoluciones.pruebaapplication;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import co.com.sersoluciones.pruebaapplication.adapters.EstudianteAdapterRecycler;
import co.com.sersoluciones.pruebaapplication.models.Estudiante;

public class ListaActivity extends AppCompatActivity implements EstudianteAdapterRecycler.OnItemClickEstudiante {

    private String[] frutas = {"banano", "manzana", "fresa", "uva", "papaya", "sandia",
            "pera", "guanabana", "uchua", "mora", "lulo", "aguacate", "mandarina", "naranja",
            "gauayaba", "mango", "feijoa"};
    private EditText editText;
    private ArrayList<Estudiante> estudiantes;
    private ArrayAdapter<Estudiante> arrayAdapter;
    private EstudianteAdapterRecycler adapterRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        editText = findViewById(R.id.editText);

        estudiantes = new ArrayList<>();
        estudiantes.add(new Estudiante("Andres", "Perez", "Quinto"));
        estudiantes.add(new Estudiante("Andres2", "Perez", "Cuarto"));
        estudiantes.add(new Estudiante("Andres3", "Perez", "Quinto"));
        estudiantes.add(new Estudiante("Andres4", "Perez", "Cuarto"));
        estudiantes.add(new Estudiante("Andres5", "Perez", "Tercero"));

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        EstudianteAdapterRecycler.OnItemClickEstudiante listener = ListaActivity.this;
        adapterRecycler = new EstudianteAdapterRecycler(estudiantes, listener);
        recyclerView.setAdapter(adapterRecycler);

        ListView listView = findViewById(R.id.list);
        arrayAdapter = new ArrayAdapter<>(ListaActivity.this,
                android.R.layout.simple_list_item_1, estudiantes);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Estudiante estudiante = (Estudiante) parent.getItemAtPosition(position);
                Intent intent = new Intent(ListaActivity.this, DetallesEstudianteActivity.class);
                intent.putExtra("estudiante", estudiante);
                startActivity(intent);
            }
        });
    }

    public void agregar(View view) {
        String texto = editText.getText().toString();
        if (!texto.isEmpty()) {
            estudiantes.add(new Estudiante(texto, "Perez", "Quinto"));
            arrayAdapter.notifyDataSetChanged();
            adapterRecycler.updateList();
        }
    }

    @Override
    public void onClickEstudiante(Estudiante estudiante) {
        Intent intent = new Intent(ListaActivity.this, DetallesEstudianteActivity.class);
        intent.putExtra("estudiante", estudiante);
        startActivity(intent);
    }
}
