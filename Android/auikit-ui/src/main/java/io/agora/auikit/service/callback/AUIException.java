package io.agora.auikit.service.callback;

public class AUIException extends Exception{
    public final int code;

    public AUIException(int code, String message){
        super(message);
        this.code = code;
    }

}
