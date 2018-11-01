package edu.gvsu.cis.jobquals;

public class GoogleUser {

    public String userId;
    public String lastSearch;

    //required default constructor
    public GoogleUser() {

    }

    public GoogleUser(String userId, String lastSearch) {
        this.userId = userId;
        this.lastSearch = lastSearch;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastSearch() {
        return lastSearch;
    }

    public void setLastSearch(String lastSearch) {
        this.lastSearch = lastSearch;
    }
}
