package org.turningme.theoretics.common.beans;

/**
 * Created by jpliu on 2020/2/23.
 */
public class UserStr2ID {
    String userIDStr;
    int userId;

    public UserStr2ID(int userId, String userIDStr) {
        this.userId = userId;
        this.userIDStr = userIDStr;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserIDStr() {
        return userIDStr;
    }

    public void setUserIDStr(String userIDStr) {
        this.userIDStr = userIDStr;
    }
}
