CREATE TABLE `sequence_id` (
  `biz_id` VARCHAR(50) NOT NULL,
  `app` VARCHAR(50) NOT NULL,
  `id` BIGINT NOT NULL,
  `start_id` BIGINT NOT NULL,
  `distance` BIGINT NOT NULL,
  `increment` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE INDEX `uniq_biz_id` (`biz_id`)
);