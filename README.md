# java-filmorate
# ER-диаграмма базы данных.
База данных основана на связях один ко многим.

![схема4](https://github.com/EvgeniyTomilov/java-filmorate/blob/add-search/src/main/resources/f-r-12.PNG)

# В проект добавлена следующая функциональность:
```roomsql
Добавление режиссёров в фильмы - ответственный Липатов Кирилл (ветка add-director);
Новая функциональность "Отзывы" - ответственный Цепелева Анна (ветка add-reviews);
Вывод самых популярных фильмов по жанру и годам – ответственный Кирпичева Екатерина (ветка add-most-populars);
Удаление фильмов и пользователей – ответственный Никитюк Вячеслав (ветка add-remove-endpoint);
Функциональность «Поиск» - ответственный Томилов Евгений (ветка add-search);
Функциональность «Общие фильмы» - ответственный Липатов Кирилл (ветка add-common-films);
Функциональность «Лента событий» - ответственный Кирпичева Екатерина (ветка add-feed);
Функциональность «Рекомендации» - ответственный Никитюк Вячеслав (ветка add-recommendations).
```


# Примеры запросов.

# 1. Вывести название, описание и продолжительность десяти фильмов с наименьшей продолжительностью
```roomsql
SELECT f.name AS film_name,
       f.description AS film_description,
       f.duration AS film_duration
FROM film AS f
ORDER BY film_duration
LIMIT 10;
```

# 2. Вывести названия всех фильмов, которые нравятся пользователю с id 13  
```roomsql
SELECT f.name
FROM user AS u
JOIN film_like AS fl ON f.film_id = fl.film_id
WHERE fl.user_id = 13;
```

# 3. Вывести все фильмы с определенным жанром 
```roomsql
SELECT f.film_id
FROM film AS f
JOIN genre AS g ON g.genre_id = f.genre_id
WHERE g.name = 'this_name';
```
