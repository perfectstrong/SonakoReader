package perfectstrong.sonako.sonakoreader.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface VolumnChapterDAO {
    @Query("SELECT * FROM chapter")
    List<Chapter> getAllChapters();

    @Query("SELECT * FROM chapter WHERE lnTitle = :lnUid")
    List<Chapter> findAllChaptersOfLightNovel(int lnUid);

    @Query("SELECT * FROM chapter WHERE lnTitle = :lnUid AND volume = :volumeId")
    List<Chapter> findAllChaptersOfVolume(int lnUid, String volumeId);

    @Query("SELECT * FROM chapter WHERE lnTitle = :lnUid AND volume = :volumeId AND id = :chapterId")
    Chapter findChapter(int lnUid, String volumeId, String chapterId);

    @Query("SELECT * FROM Volume WHERE lnTitle = :lnUid AND id = :volumeId LIMIT 1")
    Volume findVolumn(int lnUid, String volumeId);

    @Insert
    void insert(Chapter... chapters);

    @Insert
    void insert(Volume... volumes);

    @Update
    void update(Chapter... chapters);

    @Update
    void update(Volume... volumes);

    @Delete
    void delete(Chapter chapter);

    @Delete
    void delete(Volume... volumes);
}
