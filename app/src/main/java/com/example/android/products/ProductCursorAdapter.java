package com.example.android.products;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.android.products.data.ProductContract;

import static com.example.android.products.data.ProductProvider.LOG_TAG;

public class ProductCursorAdapter extends CursorAdapter {
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView qtyTextView = (TextView) view.findViewById(R.id.qty);
        TextView salesTextView = (TextView) view.findViewById(R.id.sales);
        ImageButton salesButton = (ImageButton)view.findViewById(R.id.salesButton);
        final int id = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int qtyColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int salesColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SALES);
        final Cursor cursorVal = cursor;
        final Uri uri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,id );
        Log.d(LOG_TAG, uri.toString());
        final Context mContexts =context;
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final String productQuantity = cursor.getString(qtyColumnIndex);
        String productSales = cursor.getString(salesColumnIndex);
        nameTextView.setText(productName);
        priceTextView.setText(productPrice+"$");
        qtyTextView.setText("In Stock: "+productQuantity);
        salesTextView.setText("Sold : "+productSales);
        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Quantity " + productQuantity);
                int qty = Integer.parseInt(productQuantity);
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (qty > 0) {
                    Log.d(LOG_TAG, "Click Quantity " + productQuantity);

                    String quantity = cursorVal.getString(cursorVal.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
                    String sold = cursorVal.getString(cursorVal.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SALES));
                    int quantityValue = Integer.parseInt(quantity);
                    int soldValue = Integer.parseInt(sold);
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SALES, ++soldValue);
                    Log.d(LOG_TAG, "After Click Sold " + soldValue);
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, --quantityValue);
                    Log.d(LOG_TAG, "After Click Quantity " + quantityValue);
                    int rowsAffected = resolver.update(uri, values, null, null);
                    if (rowsAffected == 0) {
                        
                        Log.d(LOG_TAG, "Failed!!");
                    } else {
                        Log.d(LOG_TAG, "Updated!!");
                    }
                    resolver.notifyChange(uri, null);
                }
            }
        });
    }
}
