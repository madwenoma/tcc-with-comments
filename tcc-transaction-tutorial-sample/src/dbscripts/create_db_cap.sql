create database `tcc_cap` /*!40100 default character set utf8 */;

use tcc_cap;

create table `cap_capital_account` (
  `capital_account_id` int(11) not null auto_increment,
  `balance_amount` decimal(10,0) default null,
  `user_id` int(11) default null,
  primary key (`capital_account_id`)
) engine=innodb auto_increment=3 default charset=utf8;

create table `cap_trade_order` (
  `id` int(11) not null auto_increment,
  `self_user_id` bigint(11) default null,
  `opposite_user_id` bigint(11) default null,
  `merchant_order_no` varchar(45) not null,
  `amount` decimal(10,0) default null,
  `status` varchar(45) default null,
  `version` int(11) default null,
  primary key (`id`),
  unique key `ux_merchant_order_no` (`merchant_order_no`)
) engine=innodb auto_increment=1 default charset=utf8;

insert into `cap_capital_account`(capital_account_id, balance_amount, user_id) value (1,10000,1000);
insert into `cap_capital_account`(capital_account_id, balance_amount, user_id) value (2,10000,2000);