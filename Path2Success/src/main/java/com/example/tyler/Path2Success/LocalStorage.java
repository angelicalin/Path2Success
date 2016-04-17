package com.example.tyler.Path2Success;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.HashSet;

/**
 * Created by pbertel on 4/7/16.
 * Referenced from http://chrisrisner.com/31-Days-of-Android--Day-23-Writing-and-Reading-Files
 */
public class LocalStorage {
    private static final String DEBUGTAG = LocalStorage.class.getSimpleName();
    public final static String FILENAME = "goal_file";
//    private JSONObject allGoals;
//    private JSONObject completedGoals;
//    private JSONObject uncompletedGoals;
    private Context appContext;

    public LocalStorage(Context c) {
//        allGoals = new JSONObject();
//        completedGoals = new JSONObject();
//        uncompletedGoals = new JSONObject();
        appContext = c;
        Log.d(DEBUGTAG, "Working!!");
    }

    public JSONObject getAllGoals() {

        JSONObject allGoals = new JSONObject();

        try {
            FileInputStream fis = appContext.openFileInput(FILENAME);
            BufferedInputStream bis = new BufferedInputStream(fis);
            StringBuffer b = new StringBuffer();
            while (bis.available() != 0) {
                char c = (char) bis.read();
                b.append(c);
            }
            bis.close();
            fis.close();
            allGoals = new JSONObject(b.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return allGoals;
    }

    //If the user passes in 'true' as the value of bool, they wish to retrieve all completed goals
    public JSONArray getCompletedOrUncompletedGoals(Boolean bool) {
        JSONArray completedOrUncompletedGoals = new JSONArray();

        JSONObject allGoals = getAllGoals();

        for (int i = 0; i < allGoals.length(); i++) {
            //Iterator code found from
            // http://stackoverflow.com/questions/13573913/android-jsonobject-how-can-i-loop-through-a-flat-json-object-to-get-each-key-a
            Iterator<String> iterator = allGoals.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                try {
                    JSONObject iteratedGoal = allGoals.getJSONObject(key);
                    Boolean completed = iteratedGoal.getBoolean("completed");
                    if (bool) {
                        if (completed) {
                            //then add the goal to the array that is to be returned
                            completedOrUncompletedGoals.put(iteratedGoal);
                        }
                    }
                    else {
                        if (!completed) {
                            completedOrUncompletedGoals.put(iteratedGoal);
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return completedOrUncompletedGoals;
    }

    public void saveGoalLocally(IndividualGoal goalToAdd) {
        try {
            JSONObject newGoal;
            String title;
            String dueDate;
            Integer category;
            Integer randInt;
            String goalID;

            title = goalToAdd.getTitle();
            dueDate = goalToAdd.getDueDate();
            category = goalToAdd.getCategory();

            newGoal = new JSONObject();
            newGoal.put("title", title);
            newGoal.put("dueDate", dueDate);
            newGoal.put("category", category);
            newGoal.put("completed", false);

            Random rand = new Random();
            randInt = rand.nextInt(10000);
            goalID = randInt.toString();

            JSONObject allGoals = getAllGoals();

            while (allGoals.has(goalID)) {
                randInt = rand.nextInt(10000);
                goalID = randInt.toString();
            }

            allGoals.put(goalID, newGoal);
            goalToAdd.setRandomID(goalID);

            //The goals JSON Object must be converted to a string before being
            // written to local storage
            String convertedGoals = allGoals.toString();

            FileOutputStream fos = appContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(convertedGoals.getBytes());
            fos.close();

//            String testFile = "hello_file";
//            String string = "hello world!";
//
//            FileOutputStream fos = appContext.openFileOutput(testFile, Context.MODE_PRIVATE);
//            fos.write(string.getBytes());
//            fos.close();
//            Log.d(DEBUGTAG, "Working!!");
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //this method is difficult, should probably work on new method of storage (uniqueID for each goal, a single JSON object, etc.)
    public void setCompleted(IndividualGoal goal, Boolean value) {
        JSONObject goalToChange;
        JSONObject allGoals;

        //need to use the parameter of "goal" in order to determine which to change

        goalToChange = new JSONObject();

        try {
            FileInputStream fis = appContext.openFileInput(FILENAME);
            BufferedInputStream bis = new BufferedInputStream(fis);
            StringBuffer b = new StringBuffer();
            while (bis.available() != 0) {
                char c = (char) bis.read();
                b.append(c);
            }
            bis.close();
            fis.close();
            allGoals = new JSONObject(b.toString());
            //goalToChange = goalList.getJSONObject(position);
            //instead, here we will get the JSON object that corresponds to the unique ID provided from the Individual Goal class

            goalToChange.put("completed", value);

            //now put the goalList back in to local storage
            String convertedGoals = allGoals.toString();

            FileOutputStream fos = appContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(convertedGoals.getBytes());
            fos.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
