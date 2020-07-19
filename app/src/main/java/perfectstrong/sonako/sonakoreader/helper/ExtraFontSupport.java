package perfectstrong.sonako.sonakoreader.helper;

import android.content.Context;
import android.graphics.Typeface;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ExtraFontSupport {
    public static final String EXTERNAL_FONT_LOCATION = Config.DEFAULT_SAVE_LOCATION + "fonts" + File.separator;
    public static final String INTERNAL_FONT_LOCATION = "file:///android_asset/fonts/";

    public static CustomFont parseCustomFontName(String encodedFontName) {
        return encodedFontName == null ? CustomFont.DEFAULT
                : encodedFontName.startsWith(CustomFont.EXTERNAL_CUSTOM_FONT_PREFIX) ?
                new CustomFont(encodedFontName.substring(CustomFont.EXTERNAL_CUSTOM_FONT_PREFIX.length()), true)
                : new CustomFont(encodedFontName.substring(CustomFont.INTERNAL_CUSTOM_FONT_PREFIX.length()), false);
    }

    public static final class CustomFont {
        public static final String EXTERNAL_CUSTOM_FONT_PREFIX = "external/";
        public static final String INTERNAL_CUSTOM_FONT_PREFIX = "internal/";
        public final String name;
        public final boolean isExternal;

        public CustomFont(String name, boolean isExternal) {
            this.name = name;
            this.isExternal = isExternal;
        }

        public boolean isDefault() {
            return Objects.equals(this, CustomFont.DEFAULT);
        }

        public Typeface getTypeFace(Context context) {
            return isDefault() ? null
                    : isExternal ? Typeface.createFromFile(EXTERNAL_FONT_LOCATION + name)
                    : Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
        }

        public String getEncodedName() {
            return isDefault() ? null
                    : isExternal ? EXTERNAL_CUSTOM_FONT_PREFIX + name
                    : INTERNAL_CUSTOM_FONT_PREFIX + name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomFont that = (CustomFont) o;
            return isExternal == that.isExternal &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, isExternal);
        }

        /**
         * Font of theme
         */
        public static final CustomFont DEFAULT = new CustomFont(null, false);

        public String getUrl() {
            assert name != null;
            return isExternal ? EXTERNAL_FONT_LOCATION + name : INTERNAL_FONT_LOCATION + name;
        }
    }

    private static List<CustomFont> AVAILABLE_INTERNAL_CHOICES;

    public static List<CustomFont> getAvailableInternalChoices() {
        return AVAILABLE_INTERNAL_CHOICES;
    }

    static {
        AVAILABLE_INTERNAL_CHOICES = Collections.unmodifiableList(Arrays.asList(
                new CustomFont("robotoslab_regular.ttf", false),
                new CustomFont("oswald_regular.ttf", false)
                // TODO
        ));
    }
}
