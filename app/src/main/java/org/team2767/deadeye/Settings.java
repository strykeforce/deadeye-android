package org.team2767.deadeye;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Pair;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Settings {

  private static final String HUE_LOW = "hue_low";
  private static final String SAT_LOW = "sat_low";
  private static final String VAL_LOW = "val_low";
  private static final String HUE_HIGH = "hue_high";
  private static final String SAT_HIGH = "sat_high";
  private static final String VAL_HIGH = "val_high";

  private final SharedPreferences prefs;

  @Inject
  public Settings(Context context) {
    prefs = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public void setHueRange(int low, int high) {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(HUE_LOW, low);
    editor.putInt(HUE_HIGH, high);
    editor.apply();
  }

  public void setSaturationRange(int low, int high) {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(SAT_LOW, low);
    editor.putInt(SAT_HIGH, high);
    editor.apply();
  }

  public void setValueRange(int low, int high) {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(VAL_LOW, low);
    editor.putInt(VAL_HIGH, high);
    editor.apply();
  }

  public Pair<Integer, Integer> getHueRange() {
    return new Pair<>(prefs.getInt(HUE_LOW, 70), prefs.getInt(HUE_HIGH, 90));
  }

  public Pair<Integer, Integer> getSaturationRange() {
    return new Pair<>(prefs.getInt(SAT_LOW, 10), prefs.getInt(SAT_HIGH, 250));
  }

  public Pair<Integer, Integer> getValueRange() {
    return new Pair<>(prefs.getInt(VAL_LOW, 10), prefs.getInt(VAL_HIGH, 250));
  }
}
