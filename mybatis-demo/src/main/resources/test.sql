create table user(
     `id` int auto_increment,
     `name` varchar(20) not null,
     `address` varchar(256) ,
     `age` int default 0,
     `birth_date` date,
     primary key (`id`)
)