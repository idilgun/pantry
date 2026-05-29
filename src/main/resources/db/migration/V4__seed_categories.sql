-- V4__seed_categories.sql
-- Seeds default ingredient categories with shelf life in days.

INSERT INTO category (name, default_shelf_life_days) VALUES
                                                         ('Fresh Greens',        5),
                                                         ('Vegetables',         10),
                                                         ('Fruits',              7),
                                                         ('Meat',                3),
                                                         ('Fish',                2),
                                                         ('Dairy',               7),
                                                         ('Butter & Cream',     14),
                                                         ('Eggs',               21),
                                                         ('Grains',            365),
                                                         ('Legumes',           365),
                                                         ('Pantry Staples',    730),
                                                         ('Spices',            730),
                                                         ('Oils',              365),
                                                         ('Sauces & Condiments', 180),
                                                         ('Canned Goods',      730),
                                                         ('Drinks',            180),
                                                         ('Bread',               4),
                                                         ('Cheese',             14);