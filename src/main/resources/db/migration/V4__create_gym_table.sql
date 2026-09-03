CREATE TABLE gym (
    id INT PRIMARY KEY,
    town_id INT NOT NULL REFERENCES town(id),
    gym_order INT NOT NULL,
    leader_ko VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    badge_ko VARCHAR(50) NOT NULL,
    description TEXT NOT NULL
);
