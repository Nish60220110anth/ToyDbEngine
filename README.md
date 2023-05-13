# ToyDbEngine

Toy database engine with custom commands

## Commands supported

1. create table `<table-name>`
2. `<col-type> ` `<col-name>` ( for adding attributes )
3. select * from `<table-name>`
4. insert into `<table-name>` (`<value>`)

## Examples

1. **Test.query**

```
create table mytab 2
int a
float b
insert into mytab (12,23.4)
create table mytab2 5
int a
float b
date d1
int b4
float sx
insert into mytab (45,78)
insert into mytab2 (4,5,6,7,8,9)
select * from mytab2
select 1 from mytab
insert into mytab (89,90)
insert into mytab (32,67)
select 4 from mytab
```

2. **Test.query.out**

```
create_table(mytab)
t1 = load_table(mytab)
add_attribute mytab int a
add_attribute mytab float b
save_table(t1)
t1 = load_table(mytab)
insert_into(t1,"(12,23.4)")
save_table(t1)
create_table(mytab2)
t2 = load_table(mytab2)
add_attribute mytab2 int a
add_attribute mytab2 float b
add_attribute mytab2 date d1
add_attribute mytab2 int b4
add_attribute mytab2 float sx
save_table(t2)
t1 = load_table(mytab)
insert_into(t1,"(45,78)")
save_table(t1)
t2 = load_table(mytab2)
insert_into(t2,"(4,5,6,7,8,9)")
save_table(t2)
fetch mytab2 *
fetch mytab 1
t1 = load_table(mytab)
insert_into(t1,"(89,90)")
save_table(t1)
t1 = load_table(mytab)
insert_into(t1,"(32,67)")
save_table(t1)
fetch mytab 4

```

## Details

> Tables are stored as csv files in `./table` folder
>
> Logs are provided for each run
>
> Intermediate code for the given query is written in `<query-file_name>.out` in the working directory.
> example : [query](https://github.com/Nish60220110anth/ToyDbEngine/blob/main/prog1.query) and [query intermediate](https://github.com/Nish60220110anth/ToyDbEngine/blob/main/prog1.query.out)

## Note

add attribute for a table command must succeed the command which created is used to create the table for which we need to add attribute
