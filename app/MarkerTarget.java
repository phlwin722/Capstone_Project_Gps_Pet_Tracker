import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MarkerTarget implements Target {

    private final MarkerOptions markerOptions;
    private final MapboxMap mapboxMap;

    public MarkerTarget(MarkerOptions markerOptions, MapboxMap mapboxMap) {
        this.markerOptions = markerOptions;
        this.mapboxMap = mapboxMap;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        // Create an Icon with the loaded bitmap and set it to the marker
        IconFactory iconFactory = IconFactory.getInstance(mapboxMap.getContext());
        Icon icon = iconFactory.fromBitmap(bitmap);
        // Remove existing marker and add a new one with the custom icon
        mapboxMap.addMarker(markerOptions.icon(icon));
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        // Handle the failure of loading the bitmap
        // Optionally, set a default icon or log the error
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        // Handle preparation for loading the bitmap
        // Optionally, set a placeholder icon
    }
}
