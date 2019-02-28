package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface LightNovelDAO {

    @Query("SELECT * FROM lightnovel")
    List<LightNovel> getAll();

    /**
     *
     * @param type select from {@link LightNovel.ProjectType}
     * @return maybe empty
     */
    @Query("SELECT * FROM lightnovel WHERE type = :type")
    List<LightNovel> getAll(String type);

    @Query("SELECT genre FROM categorization WHERE lnTitle = :lnTitle")
    List<String> getGenres(String lnTitle);

    @Query("SELECT * FROM lightnovel WHERE status = :status")
    List<LightNovel> findByStatus(String status);

    @Insert
    void insert(LightNovel... lightNovels);

    @Insert
    void insert(Categorization... categorization);

    @Update
    void update(LightNovel... lightNovels);

    @Delete
    void delete(LightNovel... lightNovels);
}
