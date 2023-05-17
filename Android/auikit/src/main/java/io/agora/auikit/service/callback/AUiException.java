package io.agora.auikit.service.callback;

public class AUiException extends Exception{
    public final int code;

    public AUiException(int code, String message){
        super(message);
        this.code = code;
    }

}
