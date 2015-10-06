--drop table products;
--drop table repos;
--drop table gitlog;

create table products (
    id            integer primary key asc,
    name          text
);

create table repos (
    id            integer primary key asc,
    product       integer,
    name          text,
    url           text
);

create table commits (
    id            integer primary key asc,
    product       integer, -- foreign key to products
    repo          integer, -- foreign key to repos
    branch        text,
    sha           text,
    message       text,
    author        text,
    date          date,
    files_changed integer,
    insertions    integer,
    deletions     integer
);

create table changed_files (
    id            integer primary key,
    commit_id     integer,
    file_name     text,
    operation     text
);

insert into products(name) values('Product #1');
insert into products(name) values('Product #2');

insert into repos(product, name, url) values ((select id from products where name='Product #1'), 'repo#1', 'git@github.com:tisnik/testrepo.git');
insert into repos(product, name, url) values ((select id from products where name='Product #1'), 'repo#2', 'git@github.com:tisnik/testrepo.ggithub.com:tisnik/testrepo.git');
insert into repos(product, name, url) values ((select id from products where name='Product #2'), 'repo#1', 'git@github.com:tisnik/testrepo.ggithub.com:tisnik/testrepo.git');

