package haha.mk_one.controldelpersonal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.concurrent.ThreadLocalRandom;

public class MarcarAsistenciasInOut extends AppCompatActivity {

    public static final String EXTRA_POSITION = "position";
    static Activity mActivity;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcar_asistencias_in_out);

        setToolBar();

        // Se obtiene la posición del item seleccionado
        int position = getIntent().getIntExtra(EXTRA_POSITION, -1);

        if (position >= 0) {
            // Carga los datos en la vista
            setupViews(position);
        } else {
            this.finish();
        }


        Window window = getWindow();
        int numeroRandom = ThreadLocalRandom.current().nextInt(0, 5);
        // Elegir transiciones

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupViews(int postion) {

        // Set Collapsing Toolbar layout to the screen
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        // Set title of Detail page
        // collapsingToolbar.setTitle(getString(R.string.item_title));

        SQLiteDatabase db = new SQLHELPER(this).getWritableDatabase();

        Resources resources = getResources();
        collapsingToolbar.setTitle("titulo");

        TextView placeDetail = (TextView) findViewById(R.id.place_detail);
        placeDetail.setText("Nombre y Apellido");

        //ImageView placePicutre = (ImageView) findViewById(R.id.country_photo);
        //placePicutre.setImageResource(R.drawable.ic_download);

    }

    private void setToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void launch(Activity context, int position, View sharedView) {
        Intent intent = new Intent(context, MarcarAsistenciasInOut.class);
        intent.putExtra(EXTRA_POSITION, position);


        // Los elementos 4, 5 y 6 usan elementos compartidos,
        if (position >= 3) {
            ActivityOptions options0 = ActivityOptions.makeSceneTransitionAnimation(context, sharedView, sharedView.getTransitionName());
            context.startActivity(intent, options0.toBundle());
        } else {
            ActivityOptions options0 = ActivityOptions.makeSceneTransitionAnimation(context);
            context.startActivity(intent, options0.toBundle());
        }
    }

}
