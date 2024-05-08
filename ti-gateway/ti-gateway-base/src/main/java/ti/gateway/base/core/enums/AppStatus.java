package ti.gateway.base.core.enums;

/**
 * APP 应用状态
 */
public enum AppStatus {

    //在线
    ONLINE(1),
    //下线
    OFF(0);

    private final int code;

    AppStatus(int code) {
        this.code = code;
    }

    public static AppStatus of(int code) {
        for (AppStatus appStatus : values()) {
            if (code == appStatus.code) {
                return appStatus;
            }
        }
        return OFF;
    }

}
