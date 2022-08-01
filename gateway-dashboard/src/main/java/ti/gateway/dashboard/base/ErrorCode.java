package ti.gateway.dashboard.base;

/**
 * 统一错误码
 */
public enum ErrorCode {

    Ok(0),
    ParameterError(400),
    InternalError(500);

    private int value;

    ErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
