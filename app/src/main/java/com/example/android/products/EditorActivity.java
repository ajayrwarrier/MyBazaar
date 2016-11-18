package com.example.android.products;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.products.data.ProductContract;
import com.example.android.products.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.example.android.products.data.ProductProvider.LOG_TAG;
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_product_LOADER = 0;
    Bitmap bitmap;
    ImageView imageView;
    private int IMAGE_REQUEST = 1;
    private Uri mCurrentproductUri;
    private EditText mNameEditText;
    private EditText mcontactEditText;
    private EditText mqtyEditText;
    private EditText mPriceEditText;
    private Button ImageAddButton;
    private String imageviewString;
    private boolean mproductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mproductHasChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        imageView = (ImageView) findViewById(R.id.image_view);
        Intent intent = getIntent();
        mCurrentproductUri = intent.getData();
        if (mCurrentproductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_product_LOADER, null, this);
        }
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mcontactEditText = (EditText) findViewById(R.id.edit_product_contact);
        mqtyEditText = (EditText) findViewById(R.id.edit_product_qty);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mNameEditText.setOnTouchListener(mTouchListener);
        mcontactEditText.setOnTouchListener(mTouchListener);
        mqtyEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        ImageAddButton = (Button) findViewById(R.id.add_imageButton);
        ImageAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                checkWriteToExternalPerms();
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
            }
        });
    }
    private void checkWriteToExternalPerms() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            cursor.close();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
                imageviewString = BitMapToString(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
    private void saveproduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String contactString = mcontactEditText.getText().toString().trim();
        String qtyString = mqtyEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String ImageString = imageviewString;
        if (mCurrentproductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(contactString) &&
                TextUtils.isEmpty(qtyString) && TextUtils.isEmpty(priceString)) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_CONTACT, contactString);
        if (ImageString == null) {
            Toast.makeText(this, getString(R.string.image_problem),
                    Toast.LENGTH_SHORT).show();
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, ImageString);
        }
        int qty = 0;
        if (!TextUtils.isEmpty(qtyString)) {
            qty = Integer.parseInt(qtyString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, qty);
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        if (mCurrentproductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentproductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentproductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Save product to database
                if (!checker()) {
                    return false;
                }
                saveproduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mproductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if (!mproductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_CONTACT,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY, ProductEntry.COLUMN_PRODUCT_IMAGE};
        return new CursorLoader(this,   // Parent activity context
                mCurrentproductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int contactColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CONTACT);
            int qtyColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndext = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int ImageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            String name = cursor.getString(nameColumnIndex);
            String contact = cursor.getString(contactColumnIndex);
            int qty = cursor.getInt(qtyColumnIndex);
            int price = cursor.getInt(priceColumnIndext);
            String image = cursor.getString(ImageColumnIndex);
            bitmap = StringToBitMap(image);
            Log.d(LOG_TAG, "Bitmap" + bitmap);
            mNameEditText.setText(name);
            mcontactEditText.setText(contact);
            mqtyEditText.setText(Integer.toString(qty));
            mPriceEditText.setText(Integer.toString(price));
            imageView.setImageBitmap(bitmap);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mcontactEditText.setText("");
        mqtyEditText.setText("");
        mPriceEditText.setText("");
        if (bitmap != null) {
            bitmap.recycle();
        }
    }
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteproduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void deleteproduct() {
        if (mCurrentproductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentproductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_prodcut_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
    private boolean checker() {
        if ((mNameEditText.getText().toString().trim().length() == 0) || (mcontactEditText.getText().toString().trim().length() == 0) ||
                (mqtyEditText.getText().toString().trim().length() == 0) ||
                (mPriceEditText.getText().toString().trim().length() == 0)||bitmap==null) {
            Toast.makeText(this, "Fill all blank fields! and add the image",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }
}