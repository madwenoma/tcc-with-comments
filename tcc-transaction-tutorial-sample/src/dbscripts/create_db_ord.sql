create database `tcc_ord` /*!40100 default character set utf8 */;
use tcc_ord;
create table `ord_order` (
  `order_id` int(11) not null auto_increment,
  `payer_user_id` int(11) default null,
  `payee_user_id` int(11) default null,
  `red_packet_pay_amount` decimal(10,0) default null,
  `capital_pay_amount` decimal(10,0) default null,
  `status` varchar(45) default null,
  `merchant_order_no` varchar(45) not null,
  `version` int(11) default null,
  primary key (`order_id`),
  unique key `merchant_order_no_unique` (`merchant_order_no`)
) engine=innodb auto_increment=1188 default charset=utf8;

create table `ord_order_line` (
  `order_line_id` int(11) not null auto_increment,
  `product_id` int(11) default null,
  `quantity` decimal(10,0) default null,
  `unit_price` decimal(10,0) default null,
  primary key (`order_line_id`)
) engine=innodb auto_increment=1 default charset=utf8;

create table `ord_shop` (
  `shop_id` int(11) not null,
  `owner_user_id` int(11) default null,
  primary key (`shop_id`)
) engine=innodb auto_increment=1 default charset=utf8;

create table `ord_product`(
  `product_id` int(11) not null,
  `shop_id` int(11) not null,
  `product_name` varchar(64) default null ,
  `price` decimal(10,0) default null,
  primary key (`product_id`)
)engine=innodb auto_increment=1 default charset=utf8;


insert into `ord_shop` (`shop_id`,`owner_user_id`) values (1,1000);

insert into `ord_product` (`product_id`,`shop_id`,`product_name`,`price`) values (1,1,'iphone6s',5288);
insert into `ord_product` (`product_id`,`shop_id`,`product_name`,`price`) values (2,1,'mac pro',10288);
insert into `ord_product` (`product_id`,`shop_id`,`product_name`,`price`) values (3,1,'iwatch',2288);