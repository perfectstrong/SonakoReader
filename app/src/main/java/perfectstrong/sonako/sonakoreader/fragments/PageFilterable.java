package perfectstrong.sonako.sonakoreader.fragments;

public interface PageFilterable {
    void filterPages(String keyword, int dateLimit);

    void showAll();
}
