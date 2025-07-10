package me.johan.shizen.auth;

public class Account {
  private String refreshToken;
  private String accessToken;
  private String username;
  private long unban;

  public Account(String refreshToken, String accessToken, String username) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.username = username;
    this.unban = 0L;
  }

  public Account(String refreshToken, String accessToken, String username, long unban) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.username = username;
    this.unban = unban;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getUsername() {
    return username;
  }

  public long getUnban() {
    return unban;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setUnban(long unban) {
    this.unban = unban;
  }
}
