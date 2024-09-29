package nya.miku.wishmaster.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import nya.miku.wishmaster.R;
import nya.miku.wishmaster.api.models.CaptchaModel;
import nya.miku.wishmaster.chans.makaba.MakabaModule;
import nya.miku.wishmaster.common.Logger;
import nya.miku.wishmaster.lib.base64.Base64;
import nya.miku.wishmaster.ui.posting.PostFormActivity;
import nya.miku.wishmaster.ui.theme.ThemeUtils;

public class WebviewCaptcha {
    protected final static String TAG = "WebviewCaptchaJS";

    CaptchaModel current;
    MakabaModule makaba;
    PostFormActivity activity;
    Boolean exceptionOccurred = false;

    @JavascriptInterface
    public void LogToLogcat(String message)
    {
        Logger.d(TAG, message);
    }

    public void SetMakabaModule(MakabaModule module)
    {
        makaba = module;
    }

    public void SetCaptchaModel(CaptchaModel model)
    {
        current = model;
    }

    public void SetPostFormActivity(PostFormActivity activity)
    {
        this.activity = activity;
    }

    @JavascriptInterface
    public String GetBase64EncodedCaptchaImage()
    {
        return ConvertToBase64(current.bitmap);
    }

    private String ConvertToBase64(Bitmap bitmap)
    {
        Bitmap bmp = bitmap;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @JavascriptInterface
    public String GetBase64EncodedButtonImages(int index)
    {
        return ConvertToBase64(AdaptButtonColors(current.emojiCaptchaButtons[index]));
    }

    @JavascriptInterface
    public int GetCaptchaButtonCount()
    {
        return current.emojiCaptchaButtons.length;
    }

    @JavascriptInterface
    public void ClickEmojiCaptcha(int index)
    {
        try {
            current = makaba.clickEmojiCaptcha(index, null, null);
            exceptionOccurred = false;
        }
        catch (Exception e) {
            exceptionOccurred = true;
        }
    }

    @JavascriptInterface
    public boolean GetEmojiCaptchaCompleted()
    {
        return current.emojiSuccess;
    }

    @JavascriptInterface
    public void Finish()
    {
        activity.endEmojiCaptcha();
    }

    @JavascriptInterface
    public void Update() throws Exception {
        try {
            SetCaptchaModel(activity.updateEmojiCaptcha());
            exceptionOccurred = false;
        }
        catch(Exception e)
        {
            exceptionOccurred = true;
        }
    }

    private int GetForegroundColorValue()
    {
        TypedValue typedValue = ThemeUtils.resolveAttribute(activity.getTheme(), android.R.attr.textColorPrimary, true);
        int color = Color.BLACK;
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            color = typedValue.data;
        } else {
            try {
                color = CompatibilityUtils.getColor(activity.getResources(), typedValue.resourceId);
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
        return color;
    }

    @JavascriptInterface
    public String GetForegroundColor()
    {
        int color = GetForegroundColorValue();
        return String.format("#%06X", (0xFFFFFF & color));
    }

    @JavascriptInterface
    public String GetBackgroundColor()
    {
        TypedValue typedValue = ThemeUtils.resolveAttribute(activity.getTheme(), android.R.attr.colorBackground, true);
        int color = Color.WHITE;
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            color = typedValue.data;
        } else {
            try {
                color = CompatibilityUtils.getColor(activity.getResources(), typedValue.resourceId);
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
        return String.format("#%06X", (0xFFFFFF & color));
    }

    private Bitmap AdaptButtonColors(Bitmap bitmap)
    {
        if (bitmap == null || !bitmap.hasAlpha()) {
            return bitmap;
        }

        int color = GetForegroundColorValue();

        Bitmap adapted = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap alpha = bitmap.extractAlpha();
        Paint paint = new Paint();
        paint.setColor(color);
        Canvas canvas = new Canvas(adapted);
        canvas.drawBitmap(alpha, 0f, 0f, paint);
        alpha.recycle();
        bitmap.recycle();
        return adapted;
    }

    @JavascriptInterface
    public void ShowErrorMessage()
    {
        Toast.makeText(activity, R.string.error_connection, Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public Boolean GetExceptionOccurred()
    {
        return exceptionOccurred;
    }
}
