package ir.sanjaghakgroup.numbercorrector;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends ActionBarActivity {
	private ProgressDialog barProgressDialog;
	private Handler updateBarHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		((CheckBox) findViewById(R.id.change_first_of_number)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				((RadioButton) findViewById(R.id.change_00_with_plus)).setEnabled(isChecked);
				((RadioButton) findViewById(R.id.change_plus_with_00)).setEnabled(isChecked);
			}
		});
		((CheckBox) findViewById(R.id.add_prefix_country_code)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				((EditText) findViewById(R.id.country_code)).setEnabled(isChecked);
			}
		});
		((Button) findViewById(R.id.dot_it)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
				alertDialog.setTitle("Are you sure?");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						start();
						alertDialog.dismiss();
					}
				});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						alertDialog.dismiss();
					}
				});
				alertDialog.show();

			}
		});
	}

	private void start() {
		boolean removeHyphne = ((CheckBox) findViewById(R.id.remove_hyphne)).isChecked();
		boolean remove_dot = ((CheckBox) findViewById(R.id.remove_dot)).isChecked();
		boolean remove_parenthesis = ((CheckBox) findViewById(R.id.remove_parenthesis)).isChecked();
		boolean remove_slashes = ((CheckBox) findViewById(R.id.remove_slashes)).isChecked();
		boolean remove_space = ((CheckBox) findViewById(R.id.remove_space)).isChecked();
		boolean remove_underscore = ((CheckBox) findViewById(R.id.remove_underscore)).isChecked();
		boolean add_prefix_country_code = ((CheckBox) findViewById(R.id.add_prefix_country_code)).isChecked();
		String countryCode = ((EditText) findViewById(R.id.country_code)).getText().toString();

		boolean change_first_of_number = ((CheckBox) findViewById(R.id.change_first_of_number)).isChecked();
		boolean change_00_with_plus;
		boolean change_plus_with_00;
		if (change_first_of_number) {
			change_00_with_plus = ((RadioButton) findViewById(R.id.change_00_with_plus)).isChecked();
			change_plus_with_00 = ((RadioButton) findViewById(R.id.change_plus_with_00)).isChecked();
		} else {
			change_00_with_plus = false;
			change_plus_with_00 = false;
		}
		NumberCorrector(removeHyphne, remove_parenthesis, remove_slashes, remove_underscore, remove_dot, remove_space, add_prefix_country_code, countryCode,
				change_plus_with_00, change_00_with_plus);
	}

	public void NumberCorrector(final boolean removeHyphen, final boolean removeParenthesis, final boolean removeSlashes, final boolean removeUnderscore,
			final boolean removeDot, final boolean removeSpace, final boolean addPrefixCountryCode, final String countryCode, final boolean changePlusWith00,
			final boolean change00withPlus) {
		barProgressDialog = new ProgressDialog(MainActivity.this);

		barProgressDialog.setTitle("Changing numbers ...");
		barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		barProgressDialog.setProgress(0);
		Cursor phones = getContentResolver().query(Phone.CONTENT_URI, new String[] { Phone.NUMBER, Phone._ID }, null, null, null);
		barProgressDialog.setMax(phones.getCount() * 2);
		phones.close();
		barProgressDialog.show();
		updateBarHandler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Cursor phones = getContentResolver().query(Phone.CONTENT_URI, new String[] { Phone.NUMBER, Phone._ID }, null, null, null);
					HashMap<Long, String> numbers = new HashMap<Long, String>();
					if (phones.moveToFirst()) {
						int column_id = phones.getColumnIndex(Phone._ID);
						int column_number = phones.getColumnIndex(Phone.NUMBER);
						do {
							String phone = phones.getString(column_number);
							long id = phones.getLong(column_id);
							numbers.put(id, phone);
							updateBarHandler.post(new Runnable() {

								@Override
								public void run() {
									barProgressDialog.incrementProgressBy(1);
								}
							});

						} while (phones.moveToNext());
					}
					phones.close();

					for (long id : numbers.keySet()) {
						String number = numbers.get(id);
						if (removeHyphen) {
							number = number.replace("-", "");
						}
						if (removeParenthesis) {
							number = number.replace(")", "");
							number = number.replace("(", "");
						}
						if (removeSlashes) {
							number = number.replace("/", "");
						}
						if (removeUnderscore) {
							number = number.replace("_", "");
						}
						if (removeDot) {
							number = number.replace(".", "");
						}
						if (removeSpace) {
							number = number.replace(" ", "");
						}
						if (addPrefixCountryCode) {
							if (!number.startsWith("00")) {
								if (!number.startsWith("0")) {
									number = "0" + number;
								}
								if (number.startsWith("0")) {
									number = number.replaceFirst("0", countryCode);
								}
							}
						}

						if (changePlusWith00) {
							if (number.startsWith("+")) {
								number = number.replaceFirst("\\+", "00");
							}
						} else if (change00withPlus) {
							if (number.startsWith("00")) {
								number = number.replaceFirst("00", "+");
							}
						}

						ContentValues contentValues = new ContentValues();
						contentValues.put(Phone.NUMBER, number);
						getContentResolver().update(Uri.withAppendedPath(Phone.CONTENT_URI, id + ""), contentValues, null, null);

						updateBarHandler.post(new Runnable() {

							@Override
							public void run() {
								barProgressDialog.incrementProgressBy(1);
							}
						});
					}
					updateBarHandler.post(new Runnable() {

						@Override
						public void run() {
							barProgressDialog.setProgress(barProgressDialog.getMax());
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					updateBarHandler.post(new Runnable() {

						@Override
						public void run() {
							barProgressDialog.dismiss();
							final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
							alertDialog.setTitle("Hooray");
							alertDialog.setMessage("Finished!!!");
							alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									alertDialog.dismiss();
								}
							});
							alertDialog.show();
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
					updateBarHandler.post(new Runnable() {

						@Override
						public void run() {
							barProgressDialog.dismiss();
							final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
							alertDialog.setTitle("Sorry...");
							alertDialog.setMessage("Finished with ERROR!");
							alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									alertDialog.dismiss();
								}
							});
							alertDialog.show();

						}
					});
				}

			}
		}).start();
	}
}
