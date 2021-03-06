package com.example.tyler.Path2Success;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class InputNewGoal extends AppCompatActivity {
    //final string to bring information to the main activity
    //an integer to indicate the category of the goal
    private Integer category;
    //private DatePicker dueDate;
    private EditText taskContent;
    private Button addButton;
    private EditText dateInput;
    private EditText categoryInput;
    private Calendar myCalendar;

    private CategoryAdapter adapter;
    private ListView categoryList;

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    private boolean putTitelIn = false;
    private boolean putDateIn = false;
    private boolean putCategoryIn = false;

    private String newCat = "";
    private ArrayList<String> categoryArray;

    private LocalStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myCalendar = Calendar.getInstance();
        setContentView(R.layout.activity_input_new_goal);
        addButton = (Button) findViewById(R.id.add_and_back);

        taskContent = (EditText) findViewById(R.id.taskContent);


        storage = new LocalStorage(this.getApplicationContext());

        categoryArray = new ArrayList<>();

        adapter = new CategoryAdapter(this, categoryArray);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.input_toolbar);
        setSupportActionBar(myToolbar);
        setTitle("Start a new goal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Handle date input
        dateInput = (EditText) findViewById(R.id.datePicker);
        dateInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (dateInput.hasFocus()) {
                    pickDate();
                    dateInput.clearFocus();
                }
            }
        });
        taskContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        dateInput.setKeyListener(null);

        //Handle category input
        categoryInput = (EditText) findViewById(R.id.categorySelector);
        categoryInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (categoryInput.hasFocus()) {
                    pickCategory();
                    categoryInput.clearFocus();

                }
            }
        });
    }

    private void initializeCategories() {
        categoryArray.clear();
        categoryArray.addAll(storage.getAllCategoriesToShow());
        categoryArray.add("Input your own");
    }


    private void pickCategory() {
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a category");
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.category_selector, null);
        builder.setView(convertView);
        categoryList = (ListView) convertView.findViewById(R.id.category_selection);
        categoryList.setAdapter(adapter);
        initializeCategories();
        adapter.notifyDataSetChanged();
        categoryList.setOnItemClickListener(new CategoryItemClickListener());
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(1200,800);
    }

    /**
     * Add a new item, and bring the information from this activity to the main activity
     *
     * @param view
     */
    public void addNewItem(View view) {
        putTitelIn = (taskContent.getText().toString().trim().length() > 0);
        if (newCat.length() != 0) {
            putCategoryIn = true;
            category = categoryArray.size()-1;
            storage.saveNewCategory(newCat);
        }
        if (putTitelIn && putDateIn && putCategoryIn) {
            Intent intent = this.getIntent();
            String task = taskContent.getText().toString();
            String date = dateInput.getText().toString();

            storage.saveNewGoal(new IndividualGoal(task,date,category));
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(InputNewGoal.this, "Cannot save goal without all options filled", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hide keyboard
     * @param view
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateLabel();
        }

    };

    private void pickDate() {
        new DatePickerDialog(InputNewGoal.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * This method will update the text in the date field to the date that is selected
     */
    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateInput.setText(sdf.format(myCalendar.getTime()).substring(0, 5));
        putDateIn = true;
    }

    private class CategoryItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        private void selectItem(int pos) {
            if(pos == categoryArray.size()-1){
                alertDialog.cancel();
                inputNewCat();
            }
            else{
                newCat = "";
                categoryInput.setText(categoryArray.get(pos));
                category=pos;
                putCategoryIn = true;
                alertDialog.cancel();
            }
        }

        private void inputNewCat() {
            AlertDialog.Builder builder = new AlertDialog.Builder(InputNewGoal.this);
            builder.setTitle("Start a new category");

            final EditText input = new EditText(InputNewGoal.this);

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setSingleLine(true);
            builder.setView(input);

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newCat = input.getText().toString();
                    categoryInput.setText(newCat);
//                    if (newCat.length() != 0) {
//                        putCategoryIn = true;
//                        category = categoryArray.size()-1;
//                        storage.saveNewCategory(newCat);
//                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

