package rsServer;

public enum MessageEnum {
    OK(200, "OK"),
    CREATED(201, "Created"),
    FORBIDDEN(403, "Forbidden")
    ;
    private Integer code;

    private String message;


    MessageEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
