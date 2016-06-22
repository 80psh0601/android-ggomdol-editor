package ggomdol.fantacydiary;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import ggomdol.ggomdoleditor.ColorPickerDialog;
import ggomdol.ggomdoleditor.GGomdolEditor;

public class WriteActivity extends AppCompatActivity {

    public static final int REQ_CODE_SELECT_IMAGE = 100;
    private GGomdolEditor mGGomdolEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        initWidget();
    }

    private void initWidget() {

        mGGomdolEditor = (GGomdolEditor) findViewById(R.id.ggomdol_et);

        findViewById(R.id.image_insert_ib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("*/*");
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            }
        });

        findViewById(R.id.save_ib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGGomdolEditor.save();
                ;
            }
        });

        findViewById(R.id.load_ib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGGomdolEditor.load();
            }
        });

        findViewById(R.id.delete_ib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGGomdolEditor.delete();
            }
        });

        findViewById(R.id.bold_tb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGGomdolEditor.setBold(((ToggleButton) v).isChecked());
            }
        });

        findViewById(R.id.italic_tb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGGomdolEditor.setItalic(((ToggleButton) v).isChecked());
            }
        });

        findViewById(R.id.underline_tb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGGomdolEditor.setUnderLine(((ToggleButton) v).isChecked());
            }
        });

        findViewById(R.id.font_size_plus_tb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGGomdolEditor.setTextsize(mGGomdolEditor.getTextsize() + 1);
                displayTextSize();
            }
        });

        findViewById(R.id.font_size_minus_tb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGGomdolEditor.setTextsize(mGGomdolEditor.getTextsize() - 1);
                displayTextSize();
            }
        });

        displayTextSize();

        findViewById(R.id.font_color_ib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ColorPickerDialog dialog = new ColorPickerDialog(WriteActivity.this, new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChange(int color) {
                        mGGomdolEditor.setTextColor(color);
                    }
                }, mGGomdolEditor.getTextColor());

                dialog.show();
            }
        });

        findViewById(R.id.background_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGGomdolEditor.setBackground();
            }
        });
    }

    private void displayTextSize() {

        final TextView fontsizeView = (TextView) findViewById(R.id.font_size_tv);
        fontsizeView.setText("" + mGGomdolEditor.getTextsize());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                mGGomdolEditor.insertMedia(data.getData());
            }
        }
    }
}
