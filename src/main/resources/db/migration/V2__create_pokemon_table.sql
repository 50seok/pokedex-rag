CREATE TABLE pokemon (
    id INT PRIMARY KEY,
    name_ko VARCHAR(50) NOT NULL,
    genus_ko VARCHAR(50) NOT NULL,
    types TEXT[] NOT NULL,
    hp INT NOT NULL,
    attack INT NOT NULL,
    defense INT NOT NULL,
    special_attack INT NOT NULL,
    special_defense INT NOT NULL,
    speed INT NOT NULL,
    sprite_url VARCHAR(255),
    flavor_text_ko TEXT NOT NULL
);
