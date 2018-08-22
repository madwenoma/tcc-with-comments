select id, self_user_id, opposite_user_id, merchant_order_no, amount, status, version from red_trade_order where merchant_order_no = 497039bb-9e66-4b09-9358-d7a0c0ef3c85

insert into red_trade_order ( self_user_id, opposite_user_id, merchant_order_no, amount, status, version )
  values ( 2000, 1000, 497039bb-9e66-4b09-9358-d7a0c0ef3c85, 12, draft, 1 )

select red_packet_account_id, balance_amount, user_id from red_red_packet_account where user_id = 2000

update red_red_packet_account set balance_amount = 400 where red_packet_account_id = 2

select red_packet_account_id, balance_amount, user_id from red_red_packet_account where user_id = 2000

select id, self_user_id, opposite_user_id, merchant_order_no, amount, status, version from red_trade_order where merchant_order_no = 497039bb-9e66-4b09-9358-d7a0c0ef3c85

update red_trade_order set status = confirm, version = 2 where id = 2 and version = 2-1 

select red_packet_account_id, balance_amount, user_id from red_red_packet_account where user_id = 1000

update red_red_packet_account set balance_amount = 1050 where red_packet_account_id = 1 

select red_packet_account_id, balance_amount, user_id from red_red_packet_account where user_id = 2000


==================================================================================================

SELECT id, self_user_id, opposite_user_id, merchant_order_no, amount, status, version FROM cap_trade_order WHERE merchant_order_no = ? 

INSERT INTO cap_trade_order ( self_user_id, opposite_user_id, merchant_order_no, amount, status, version ) VALUES ( ?, ?, ?, ?, ?, ? ) 

SELECT capital_account_id, balance_amount, user_id FROM cap_capital_account WHERE user_id = ? 

UPDATE cap_capital_account SET balance_amount = balance_amount+-2276 WHERE capital_account_id = 2 AND balance_amount+-2276>=0 

SELECT capital_account_id, balance_amount, user_id FROM cap_capital_account WHERE user_id = 2000

SELECT id, self_user_id, opposite_user_id, merchant_order_no, amount, status, version FROM cap_trade_order WHERE merchant_order_no = 497039bb-9e66-4b09-9358-d7a0c0ef3c85

UPDATE cap_trade_order SET status = CONFIRM, version = 2 WHERE id = 2 AND version = 2-1 

SELECT capital_account_id, balance_amount, user_id FROM cap_capital_account WHERE user_id = 1000

UPDATE cap_capital_account SET balance_amount = balance_amount+? WHERE capital_account_id = ? AND balance_amount+?>=0 

SELECT capital_account_id, balance_amount, user_id FROM cap_capital_account WHERE user_id = ?  

===================================================================================================

select shop_id, owner_user_id from ord_shop where shop_id = 1

select product_id, shop_id, product_name, price from ord_product where product_id = 3 

insert into ord_order ( payer_user_id, payee_user_id, red_packet_pay_amount, capital_pay_amount, status, merchant_order_no, version ) values ( 2000,1000,null,null,DRAFT,497039bb-9e66-4b09-9358-d7a0c0ef3c85,1)

insert into ord_order_line ( product_id, quantity, unit_price ) values ( 3, 1, 2288 ) 

update ord_order set status = PAYING, red_packet_pay_amount = 12, capital_pay_amount = 2276, version = 2 where order_id = 1191 and version=2-1 

select order_id, payer_user_id, payee_user_id, red_packet_pay_amount, capital_pay_amount, status, merchant_order_no, version from ord_order where merchant_order_no = 497039bb-9e66-4b09-9358-d7a0c0ef3c85 

select order_id, payer_user_id, payee_user_id, red_packet_pay_amount, capital_pay_amount, status, merchant_order_no, version from ord_order where merchant_order_no = 497039bb-9e66-4b09-9358-d7a0c0ef3c85

update ord_order set status = CONFIRMED, red_packet_pay_amount = 12, capital_pay_amount = 2276, version = 3 where order_id = 1191 and version=3-1 

select order_id, payer_user_id, payee_user_id, red_packet_pay_amount, capital_pay_amount, status, merchant_order_no, version from ord_order where merchant_order_no = 497039bb-9e66-4b09-9358-d7a0c0ef3c85

