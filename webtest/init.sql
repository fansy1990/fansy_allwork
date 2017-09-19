
-- create database
create database hdfs_hbase;

-- create table
CREATE TABLE `hdfsuser` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `authority` int(11) NOT NULL,
  `email` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


-- insert data
INSERT INTO `hdfsuser` VALUES (1, 1, 'fansy1@qq.com', 'fansy1', '123456');
INSERT INTO `hdfsuser` VALUES (2, 0, 'fansy@qq.com', 'fansy', '123456');
