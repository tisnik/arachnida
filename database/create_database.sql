-- We don't have to drop tables because DB is empty at the beginning
--drop table products;
--drop table repos;
--drop table branches_for_commit;
--drop table commits;
--drop table changed_files;

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

create table branches_for_commit (
    id            integer primary key asc,
    commit_id     integer, -- foreign key to commits
    branch        text
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

