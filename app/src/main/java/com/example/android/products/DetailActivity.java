package com.example.android.products;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.products.data.ProductContract;
import static com.example.android.products.data.ProductProvider.LOG_TAG;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;
    TextView nameView;
    TextView priceView;
    TextView qtyView;
    Bitmap bitmap;
    TextView contactView;
    TextView salesView ;
    ImageView imageView;
    Button Edit;
    Button Delete;
    Uri uri;
    Button Order;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final Intent intent = getIntent();
        Bundle b = new Bundle();
        b = getIntent().getExtras();
        final long id = b.getLong("id");
        final Uri mCurrentProductUri = intent.getData();
        uri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,id );
        Log.d(LOG_TAG, uri.toString());
        nameView = (TextView)findViewById(R.id.name);
        priceView = (TextView)findViewById(R.id.price);
        qtyView = (TextView)findViewById(R.id.stock);
        contactView=(TextView)findViewById(R.id.contact);
        salesView = (TextView)findViewById(R.id.sale);
        imageView =(ImageView)findViewById(R.id.image_view);
        Edit =(Button)findViewById(R.id.edit);
        Delete =(Button) findViewById(R.id.delete);
        Order =(Button)findViewById(R.id.order);
        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(DetailActivity.this,EditorActivity.class);
                intent1.setData(mCurrentProductUri);
                startActivity(intent1);
            }
        });
        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });
        Order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); 
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{contactView.getText().toString()});
                intent.putExtra(Intent.EXTRA_SUBJECT, "ORDER MORE ITEM");
                intent.putExtra(Intent.EXTRA_TEXT, "Product ID: " + id + "\n" + "Product name: " + nameView.getText().toString());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            }
        });
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_CONTACT,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, ProductContract.ProductEntry.COLUMN_PRODUCT_SALES, ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE};
        return new CursorLoader(this,
                uri,         
                projection,             
                null,                   
                null,                   
                null);                  
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOG_TAG, "LOAD FINISHED");
        if (cursor == null || cursor.getCount() < 1) {
            Log.d(LOG_TAG, "NULL");
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int contactColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_CONTACT);
            int qtyColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int salesColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SALES);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
            String name = cursor.getString(nameColumnIndex);
            String contact = cursor.getString(contactColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            int qty = cursor.getInt(qtyColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int sales = cursor.getInt(salesColumnIndex);
            Log.d(LOG_TAG, name);
            bitmap = StringToBitMap(image);
            contactView.setText(contact);
            qtyView.setText(String.valueOf(qty));
            priceView.setText(String.valueOf(price));
            nameView.setText(name);
            salesView.setText(String.valueOf(sales));
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            contactView.setText("");
        qtyView.setText("");
        priceView.setText("");
        nameView.setText("");
        salesView.setText("");
        if (bitmap!=null){
        bitmap.recycle();}
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
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
    private void deleteProduct() {
        if (uri != null) {
            int rowsDeleted = getContentResolver().delete(uri, null, null);
            if (rowsDeleted == 0) {
                
                Toast.makeText(this, getString(R.string.editor_delete_prodcut_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
