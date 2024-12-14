import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public class MarkerTargetPet implements Target {
    private final MarkerOptions markerOptions;
    private final MapboxMap mapboxMap;

    public MarkerTarget(MarkerOptions markerOptions, MapboxMap mapboxMap) {
        this.markerOptions = markerOptions;
        this.mapboxMap = mapboxMap;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        IconFactory iconFactory = IconFactory.getInstance(mapboxMap.getContext());
        Icon icon = iconFactory.fromBitmap(bitmap);
        mapboxMap.addMarker(markerOptions.icon(icon));
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        // Handle the failure of loading the bitmap
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        // Handle preparation for loading the bitmap
    }
}
