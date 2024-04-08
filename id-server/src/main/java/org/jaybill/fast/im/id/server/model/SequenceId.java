package org.jaybill.fast.im.id.server.model;

import lombok.*;

import java.util.Date;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SequenceId {

    /**
     * business identify, unique
     */
    private String bizId;

    /**
     * Next id to be allocated
     */
    private Long id;

    /**
     * start id, include
     */
    private Long startId;

    /**
     * endId = startId + distance, exclude
     */
    private Long distance;

    /**
     * When id>=endId, update id and startId again with increment
     */
    private Long increment;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;
}
