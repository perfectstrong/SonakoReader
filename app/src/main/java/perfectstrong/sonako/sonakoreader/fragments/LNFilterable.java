package perfectstrong.sonako.sonakoreader.fragments;

import perfectstrong.sonako.sonakoreader.database.LightNovel;

public interface LNFilterable {
    /**
     * Filter list of lightnovel
     *  @param keyword in title
     * @param type    {@link LightNovel.ProjectType}
     * @param status  {@link LightNovel.ProjectStatus}
     * @param genres  {@link LightNovel.ProjectGenre}
     */
    void filterLNList(String keyword,
                      String type,
                      String status,
                      String[] genres);

    /**
     * Reset
     */
    void showAll();
}
