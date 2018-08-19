create database `tcc_red` /*!40100 default character set utf8 */;
use tcc_red;
create table `red_red_packet_account` (
  `red_packet_account_id` int(11) not null,
  `balance_amount` decimal(10,0) default null,
  `user_id` int(11) default null,
  primary key (`red_packet_account_id`)
) engine=innodb auto_increment=1 default charset=utf8;

create table `red_trade_order` (
  `id` int(11) not null auto_increment,
  `self_user_id` bigint(11) default null,
  `opposite_user_id` bigint(11) default null,
  `merchant_order_no` varchar(45) not null,
  `amount` decimal(10,0) default null,
  `status` varchar(45) default null,
  `version` int(11) default null,
  primary key (`id`),
  unique key `merchant_order_no_unique` (`merchant_order_no`)
) engine=innodb auto_increment=1 default charset=utf8;

insert into `red_red_packet_account` (`red_packet_account_id`,`balance_amount`,`user_id`) values (1,950,1000);
insert into `red_red_packet_account` (`red_packet_account_id`,`balance_amount`,`user_id`) values (2,500,2000);
