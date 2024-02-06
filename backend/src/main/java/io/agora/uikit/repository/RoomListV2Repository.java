package io.agora.uikit.repository;

import io.agora.uikit.bean.entity.RoomListV2Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomListV2Repository extends MongoRepository<RoomListV2Entity, String> {
    Page<RoomListV2Entity> findByCreateTimeLessThanAndAppIdAndSceneId(Long createTime, String appId, String sceneId, Pageable pageAble);

    List<RoomListV2Entity> findByCreateTimeLessThan(Long createTime);

    @Query("{_id : ?0}")
    void updatById(String id, Update update);
}
