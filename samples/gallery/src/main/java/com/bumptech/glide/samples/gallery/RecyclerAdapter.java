package com.bumptech.glide.samples.gallery;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;
import static com.bumptech.glide.request.RequestOptions.signatureOf;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.signature.MediaStoreSignature;

import java.util.Collections;
import java.util.List;

/**
 * Displays {@link com.bumptech.glide.samples.gallery.MediaStoreData} in a recycler view.
 */
class RecyclerAdapter extends RecyclerView.Adapter
    implements ListPreloader.PreloadSizeProvider<MediaStoreData>,
    ListPreloader.PreloadModelProvider<MediaStoreData> {

  private final List<MediaStoreData> data;
  private final int screenWidth;
  private final RequestBuilder<Drawable> requestBuilder;

  private int[] actualDimensions;

  RecyclerAdapter(Context context, List<MediaStoreData> data, RequestManager requestManager) {
    this.data = data;
    requestBuilder = requestManager
        .asDrawable()
        .apply(fitCenterTransform(context));

    setHasStableIds(true);

    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    screenWidth = display.getWidth();
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
    final View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.recycler_item, viewGroup, false);
    view.getLayoutParams().width = screenWidth;

    if (actualDimensions == null) {
      view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          if (actualDimensions == null) {
            actualDimensions = new int[] { view.getWidth(), view.getHeight() };
          }
          view.getViewTreeObserver().removeOnPreDrawListener(this);
          return true;
        }
      });
    }

    return new ListViewHolder(view);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    MediaStoreData current = data.get(position);

    final ListViewHolder vh = (ListViewHolder) viewHolder;

    Key signature =
        new MediaStoreSignature(current.mimeType, current.dateModified, current.orientation);

    requestBuilder
        .clone()
        .apply(signatureOf(signature))
        .load(current.uri)
        .into(vh.image);
  }

  @Override
  public long getItemId(int position) {
    return data.get(position).rowId;
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  @Override
  public int getItemViewType(int position) {
    return 0;
  }

  @Override
  public List<MediaStoreData> getPreloadItems(int position) {
    return Collections.singletonList(data.get(position));
  }

  @Override
  public RequestBuilder getPreloadRequestBuilder(MediaStoreData item) {
    MediaStoreSignature signature =
        new MediaStoreSignature(item.mimeType, item.dateModified, item.orientation);
    return requestBuilder
        .clone()
        .apply(signatureOf(signature))
        .load(item.uri);
  }

  @Override
  public int[] getPreloadSize(MediaStoreData item, int adapterPosition, int perItemPosition) {
    return actualDimensions;
  }

  /**
   * ViewHolder containing views to display individual {@link com.bumptech.glide.samples
   * .gallery.MediaStoreData}.
   */
  public static final class ListViewHolder extends RecyclerView.ViewHolder {

    private final ImageView image;

    public ListViewHolder(View itemView) {
      super(itemView);
      image = (ImageView) itemView.findViewById(R.id.image);
    }
  }
}
