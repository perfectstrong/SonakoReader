package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface LightNovelDAO {

    @Query("SELECT * FROM lightnovel")
    List<LightNovel> getAll();

    @Query("SELECT genre FROM categorization WHERE lnTitle = :lnTitle")
    List<String> getGenres(String lnTitle);

    @Insert
    void insert(LightNovel... lightNovels);

    @Insert
    void insert(Categorization... categorization);

    @Update
    void update(LightNovel... lightNovels);
}
