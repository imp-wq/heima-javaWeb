
DROP TABLE IF EXISTS account;

-- 创建账户表
CREATE TABLE account(
	id int PRIMARY KEY auto_increment,
	name varchar(10),
	money double(10,2)
);

-- 添加数据
INSERT INTO account(name,money) values('张三',1000),('李四',1000);

SELECT * FROM account;
UPDATE account SET money=1000;

BEGIN;
UPDATE account SET money=money-500 WHERE `NAME`='李四';
出错了...
UPDATE account SET money=money+500 WHERE `NAME`='张三';
ROLLBACK;
COMMIT;