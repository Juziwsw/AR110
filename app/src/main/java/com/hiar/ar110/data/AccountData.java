package com.hiar.ar110.data;

/**
 * @author tangxucheng
 * @date 2021/6/15
 * Email: xucheng.tang@hiscene.com
 */
public class AccountData {
    public String token;
    public long emergencyId;

    @Override
    public String toString() {
        return "AccountData{" +
            "token='" + token + '\'' +
            ", emergencyId=" + emergencyId +
            '}';
    }
}
