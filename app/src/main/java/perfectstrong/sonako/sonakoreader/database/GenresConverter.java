package perfectstrong.sonako.sonakoreader.database;

import java.util.Arrays;
import java.util.List;

import androidx.room.TypeConverter;

@SuppressWarnings("WeakerAccess")
public class GenresConverter {

    public static final String SEPARATOR = ",";
    public static final String REPLACEMENT = " ";

    @TypeConverter
    public List<String> fromCompressedString(String genres) {
        if (genres == null)
            return null;
        else
            return Arrays.asList(genres.split(SEPARATOR));
    }

    @TypeConverter
    public String fromList(List<String> genres) {
        if (genres == null || genres.isEmpty()) return null;
        StringBuilder str = new StringBuilder(genres.get(0));
        for (int i = 1; i < genres.size(); i++) {
            str.append(SEPARATOR)
                    .append(genres.get(i).replace(SEPARATOR, REPLACEMENT));
        }
        return str.toString();
    }
}
