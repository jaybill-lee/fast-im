package org.jaybill.fast.im.id.server.model;

import lombok.*;

import java.util.Date;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SequenceId {

    private String bizId;

    private Long id;

    private Long startId;

    private Long distance;

    private Long increment;

    private Date createTime;

    private Date updateTime;
}
