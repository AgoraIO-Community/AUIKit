package io.agora.uikit.bean.req;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CreateKickOutRule {
    @SerializedName("appid")
    private String appId;
    @SerializedName("cname")
    private String cname;
    @SerializedName("uid")
    private Long uid;
    @SerializedName("ip")
    private String ip;
    @SerializedName("time")
    private Integer time;
    @SerializedName("privileges")
    private List<String> privileges;
}
