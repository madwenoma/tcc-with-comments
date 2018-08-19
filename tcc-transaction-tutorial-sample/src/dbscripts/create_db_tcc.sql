create database `tcc` /*!40100 default character set utf8 */;
use tcc;
create table `tcc_transaction_cap` (
  `transaction_id` int(11) not null auto_increment,
  `domain` varchar(100) default null,
  `global_tx_id` varbinary(32) not null,
  `branch_qualifier` varbinary(32) not null,
  `content` varbinary(8000) default null,
  `status` int(11) default null,
  `transaction_type` int(11) default null,
  `retried_count` int(11) default null,
  `create_time` datetime default null,
  `last_update_time` datetime default null,
  `version` int(11) default null,
  primary key (`transaction_id`),
  unique key `ux_tx_bq` (`global_tx_id`,`branch_qualifier`)
) engine=innodb default charset=utf8;

create table `tcc_transaction_ord` (
  `transaction_id` int(11) not null auto_increment,
  `domain` varchar(100) default null,
  `global_tx_id` varbinary(32) not null,
  `branch_qualifier` varbinary(32) not null,
  `content` varbinary(8000) default null,
  `status` int(11) default null,
  `transaction_type` int(11) default null,
  `retried_count` int(11) default null,
  `create_time` datetime default null,
  `last_update_time` datetime default null,
  `version` int(11) default null,
  primary key (`transaction_id`),
  unique key `ux_tx_bq` (`global_tx_id`,`branch_qualifier`)
) engine=innodb default charset=utf8;

create table `tcc_transaction_red` (
  `transaction_id` int(11) not null auto_increment,
  `domain` varchar(100) default null,
  `global_tx_id` varbinary(32) not null,
  `branch_qualifier` varbinary(32) not null,
  `content` varbinary(8000) default null,
  `status` int(11) default null,
  `transaction_type` int(11) default null,
  `retried_count` int(11) default null,
  `create_time` datetime default null,
  `last_update_time` datetime default null,
  `version` int(11) default null,
  primary key (`transaction_id`),
  unique key `ux_tx_bq` (`global_tx_id`,`branch_qualifier`)
) engine=innodb default charset=utf8;


create table `tcc_transaction_ut` (
  `transaction_id` int(11) not null auto_increment,
  `domain` varchar(100) default null,
  `global_tx_id` varbinary(32) not null,
  `branch_qualifier` varbinary(32) not null,
  `content` varbinary(8000) default null,
  `status` int(11) default null,
  `transaction_type` int(11) default null,
  `retried_count` int(11) default null,
  `create_time` datetime default null,
  `last_update_time` datetime default null,
  `version` int(11) default null,
  primary key (`transaction_id`),
  unique key `ux_tx_bq` (`global_tx_id`,`branch_qualifier`)
) engine=innodb default charset=utf8;